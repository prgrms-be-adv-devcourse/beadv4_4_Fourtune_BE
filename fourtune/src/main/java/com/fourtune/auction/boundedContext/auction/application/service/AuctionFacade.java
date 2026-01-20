package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.shared.auction.dto.AuctionItemCreateRequest;
import com.fourtune.auction.shared.auction.dto.AuctionItemDetailResponse;
import com.fourtune.auction.shared.auction.dto.AuctionItemResponse;
import com.fourtune.auction.shared.auction.dto.AuctionItemUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 경매 Facade
 * - 여러 UseCase를 조합하여 복잡한 비즈니스 플로우 처리
 * - Controller는 이 Facade만 호출
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuctionFacade {

    private final AuctionCreateUseCase auctionCreateUseCase;
    private final AuctionUpdateUseCase auctionUpdateUseCase;
    private final AuctionDeleteUseCase auctionDeleteUseCase;
    private final AuctionCloseUseCase auctionCloseUseCase;
    private final AuctionExtendUseCase auctionExtendUseCase;
    private final AuctionQueryUseCase auctionQueryUseCase;
    private final AuctionBuyNowUseCase auctionBuyNowUseCase;

    /**
     * 경매 생성
     */
    public AuctionItemResponse createAuction(Long sellerId, AuctionItemCreateRequest request, List<?> images) {
        // 1. 이미지 업로드는 MVP에서 제외 (Mock URL 사용)
        // TODO: S3Service로 이미지 업로드 추가 필요
        
        // 2. AuctionCreateUseCase 호출
        Long auctionId = auctionCreateUseCase.createAuction(sellerId, request);
        
        // 3. 생성된 경매 조회 및 DTO 변환
        return auctionQueryUseCase.getAuctionById(auctionId);
    }

    /**
     * 경매 수정
     */
    public AuctionItemResponse updateAuction(Long auctionId, Long userId, AuctionItemUpdateRequest request) {
        // 1. AuctionUpdateUseCase 호출
        auctionUpdateUseCase.updateAuction(auctionId, userId, request);
        
        // 2. 수정된 경매 조회 및 DTO 변환
        return auctionQueryUseCase.getAuctionById(auctionId);
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
        // 1. 종료 시간이 지난 경매 목록 조회
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.List<com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem> expiredAuctions =
                auctionQueryUseCase.findExpiredAuctions(now);
        
        // 2. 각 경매마다 AuctionCloseUseCase 호출
        for (com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auction : expiredAuctions) {
            try {
                auctionCloseUseCase.closeAuction(auction.getId());
            } catch (Exception e) {
                // 로깅만 하고 계속 진행 (한 경매 실패가 전체에 영향 없도록)
                // TODO: 로깅 추가
            }
        }
    }

    /**
     * 경매 상세 조회
     */
    @Transactional(readOnly = true)
    public AuctionItemDetailResponse getAuctionDetail(Long auctionId) {
        // 1. AuctionQueryUseCase.getAuctionDetail 호출
        AuctionItemDetailResponse response = auctionQueryUseCase.getAuctionDetail(auctionId);
        
        // 2. 조회수 증가는 별도 엔드포인트에서 처리 (PATCH /auctions/{id}/view)
        // TODO: 비동기 처리로 변경 고려
        
        return response;
    }

    /**
     * 경매 목록 조회 (필터링)
     */
    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getAuctionList(
            com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus status,
            com.fourtune.auction.boundedContext.auction.domain.constant.Category category,
            Pageable pageable) {
        // AuctionQueryUseCase 호출 (status, category 필터링 적용)
        return auctionQueryUseCase.getAuctionList(status, category, pageable);
    }

    /**
     * 판매자의 경매 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getSellerAuctions(Long sellerId, Pageable pageable) {
        // AuctionQueryUseCase 호출
        return auctionQueryUseCase.getSellerAuctions(sellerId, pageable);
    }

    /**
     * 즉시구매 처리 (경매 상세 페이지에서 "즉시구매" 버튼)
     */
    public String executeBuyNow(Long auctionId, Long buyerId) {
        // TODO: 구현 필요
        // 1. AuctionBuyNowUseCase.executeBuyNow 호출
        // 2. orderId 반환 (결제 페이지로 리다이렉트)
        return null;
    }

    /**
     * 조회수 증가
     */
    public void increaseViewCount(Long auctionId) {
        // AuctionQueryUseCase.increaseViewCount 호출
        auctionQueryUseCase.increaseViewCount(auctionId);
        // TODO: Redis 캐싱 및 비동기 처리는 나중에 추가
    }

}
