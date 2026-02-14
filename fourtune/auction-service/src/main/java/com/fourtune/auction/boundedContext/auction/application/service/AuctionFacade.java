package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.common.shared.auction.dto.AuctionItemCreateRequest;
import com.fourtune.common.shared.auction.dto.AuctionItemDetailResponse;
import com.fourtune.common.shared.auction.dto.AuctionItemResponse;
import com.fourtune.common.shared.auction.dto.AuctionItemUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 경매 Facade
 * - 여러 UseCase를 조합하여 복잡한 비즈니스 플로우 처리
 * - Controller는 이 Facade만 호출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionFacade {

    private final AuctionCreateUseCase auctionCreateUseCase;
    private final AuctionUpdateUseCase auctionUpdateUseCase;
    private final AuctionDeleteUseCase auctionDeleteUseCase;
    private final AuctionCloseUseCase auctionCloseUseCase;
    private final AuctionExtendUseCase auctionExtendUseCase;
    private final AuctionQueryUseCase auctionQueryUseCase;
    private final AuctionBuyNowUseCase auctionBuyNowUseCase;
    private final AuctionStartUseCase auctionStartUseCase;
    private final RedisViewCountService redisViewCountService;

    @Value("${app.view-count.use-redis:true}")
    private boolean viewCountUseRedis;

    /**
     * 경매 생성
     */
    public AuctionItemResponse createAuction(Long sellerId, AuctionItemCreateRequest request, List<?> images) {
        // 1. 이미지 업로드는 MVP에서 제외 (Mock URL 사용)
        // TODO: S3Service로 이미지 업로드 추가 필요

        // 2. AuctionCreateUseCase 호출
        Long auctionId = auctionCreateUseCase.createAuction(sellerId, request);

        // 3. 생성된 경매 조회 및 DTO 변환 (readOnly 트랜잭션으로 분리)
        return getAuctionByIdInReadOnlyTransaction(auctionId);
    }

    /**
     * 경매 수정
     */
    public AuctionItemResponse updateAuction(Long auctionId, Long userId, AuctionItemUpdateRequest request) {
        // 1. AuctionUpdateUseCase 호출
        auctionUpdateUseCase.updateAuction(auctionId, userId, request);

        // 2. 수정된 경매 조회 및 DTO 변환 (readOnly 트랜잭션으로 분리)
        return getAuctionByIdInReadOnlyTransaction(auctionId);
    }

    /**
     * 경매 삭제
     */
    public void deleteAuction(Long auctionId, Long userId) {
        // AuctionDeleteUseCase 호출
        auctionDeleteUseCase.deleteAuction(auctionId, userId);
    }

    /**
     * 경매 종료 처리 (스케줄러에서 호출)
     */
    public void closeExpiredAuctions() {
        // 1. 종료 시간이 지난 경매 목록 조회 (readOnly 트랜잭션으로 분리)
        List<com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem> expiredAuctions = findExpiredAuctionsInReadOnlyTransaction();

        // 2. 각 경매마다 독립 트랜잭션으로 종료 처리
        for (com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auction : expiredAuctions) {
            try {
                closeAuctionInNewTransaction(auction.getId());
            } catch (Exception e) {
                log.error("경매 종료 실패: auctionId={}", auction.getId(), e);
            }
        }
    }

    /**
     * 만료된 경매 목록 조회 (읽기 전용 트랜잭션)
     */
    public List<com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem> findExpiredAuctionsInReadOnlyTransaction() {
        LocalDateTime now = LocalDateTime.now();
        return auctionQueryUseCase.findExpiredAuctions(now);
    }

    /**
     * 경매 종료 처리 (독립 트랜잭션)
     * 각 경매마다 독립적인 트랜잭션으로 처리하여 하나 실패 시 다른 것들에 영향 없도록 함
     */
    public void closeAuctionInNewTransaction(Long auctionId) {
        auctionCloseUseCase.closeAuction(auctionId);
    }

    /**
     * 경매 상세 조회
     */
    @Transactional(readOnly = true)
    public AuctionItemDetailResponse getAuctionDetail(Long auctionId) {
        AuctionItemDetailResponse response = auctionQueryUseCase.getAuctionDetail(auctionId);
        if (viewCountUseRedis) {
            long combined = redisViewCountService.getViewCount(auctionId, response.viewCount());
            response = response.withViewCount(combined);
        }
        return response;
    }

    /**
     * 경매 ID로 조회 (읽기 전용 트랜잭션)
     */
    public AuctionItemResponse getAuctionByIdInReadOnlyTransaction(Long auctionId) {
        return auctionQueryUseCase.getAuctionById(auctionId);
    }

    /**
     * 경매 목록 조회 (필터링)
     */
    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getAuctionList(
            com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus status,
            com.fourtune.auction.boundedContext.auction.domain.constant.Category category,
            Pageable pageable) {
        Page<AuctionItemResponse> page = auctionQueryUseCase.getAuctionList(status, category, pageable);
        if (viewCountUseRedis && !page.isEmpty()) {
            List<AuctionItemResponse> content = page.getContent();
            List<Long> ids = content.stream().map(AuctionItemResponse::id).toList();
            Map<Long, Long> dbMap = content.stream().collect(Collectors.toMap(AuctionItemResponse::id, r -> r.viewCount() != null ? r.viewCount() : 0L));
            Map<Long, Long> combined = redisViewCountService.getViewCounts(ids, dbMap);
            List<AuctionItemResponse> newContent = content.stream()
                    .map(r -> r.withViewCount(combined.getOrDefault(r.id(), r.viewCount() != null ? r.viewCount() : 0L)))
                    .toList();
            return new PageImpl<>(newContent, page.getPageable(), page.getTotalElements());
        }
        return page;
    }

    /**
     * 판매자의 경매 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getSellerAuctions(Long sellerId, Pageable pageable) {
        Page<AuctionItemResponse> page = auctionQueryUseCase.getSellerAuctions(sellerId, pageable);
        if (viewCountUseRedis && !page.isEmpty()) {
            List<AuctionItemResponse> content = page.getContent();
            List<Long> ids = content.stream().map(AuctionItemResponse::id).toList();
            Map<Long, Long> dbMap = content.stream().collect(Collectors.toMap(AuctionItemResponse::id, r -> r.viewCount() != null ? r.viewCount() : 0L));
            Map<Long, Long> combined = redisViewCountService.getViewCounts(ids, dbMap);
            List<AuctionItemResponse> newContent = content.stream()
                    .map(r -> r.withViewCount(combined.getOrDefault(r.id(), r.viewCount() != null ? r.viewCount() : 0L)))
                    .toList();
            return new PageImpl<>(newContent, page.getPageable(), page.getTotalElements());
        }
        return page;
    }

    /**
     * 즉시구매 처리 (경매 상세 페이지에서 "즉시구매" 버튼)
     */
    public String executeBuyNow(Long auctionId, Long buyerId) {
        // AuctionBuyNowUseCase.executeBuyNow 호출
        // orderId 반환 (결제 페이지로 리다이렉트)
        return auctionBuyNowUseCase.executeBuyNow(auctionId, buyerId);
    }

    /**
     * 조회수 증가 (Redis 사용 시 INCR, 미사용 시 DB 직접 증가)
     */
    public void increaseViewCount(Long auctionId) {
        if (viewCountUseRedis) {
            redisViewCountService.incrementViewCount(auctionId);
        } else {
            auctionQueryUseCase.increaseViewCount(auctionId);
        }
    }

    public void startAuctionInTransaction(Long auctionId) {
        auctionStartUseCase.startAuctionInTransaction(auctionId);
    }

}
