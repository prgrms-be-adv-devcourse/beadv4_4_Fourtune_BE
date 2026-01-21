package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.shared.auction.dto.AuctionItemDetailResponse;
import com.fourtune.auction.shared.auction.dto.AuctionItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 경매 조회 UseCase
 * - 경매 상세 조회
 * - 경매 목록 조회 (페이징, 필터링)
 * - 조회수 증가 (Redis)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionQueryUseCase {

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;
    // private final RedisService redisService; // TODO: 나중에 추가

    /**
     * 경매 ID로 조회 (간단 버전)
     */
    public AuctionItemResponse getAuctionById(Long auctionId) {
        com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem =
                auctionSupport.findByIdOrThrow(auctionId);
        return AuctionItemResponse.from(auctionItem);
    }

    /**
     * 경매 상세 조회
     */
    public AuctionItemDetailResponse getAuctionDetail(Long auctionId) {
        // 1. 경매 조회
        com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem =
                auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. DTO 변환 후 반환
        return AuctionItemDetailResponse.from(auctionItem);
    }

    /**
     * 경매 목록 조회 (페이징)
     */
    public Page<AuctionItemResponse> getAuctionList(
            com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus status,
            com.fourtune.auction.boundedContext.auction.domain.constant.Category category,
            Pageable pageable
    ) {
        // 1. 경매 목록 조회 (상태, 카테고리 필터링)
        Page<com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem> auctionPage;
        
        if (status != null && category != null) {
            auctionPage = auctionSupport.findByStatusAndCategory(status, category, pageable);
        } else if (status != null) {
            auctionPage = auctionSupport.findByStatus(status, pageable);
        } else {
            // 필터 없으면 전체 조회
            auctionPage = auctionSupport.findAll(pageable);
        }
        
        // 2. DTO 변환 후 반환
        return auctionPage.map(AuctionItemResponse::from);
    }

    /**
     * 판매자의 경매 목록 조회
     */
    public Page<AuctionItemResponse> getSellerAuctions(Long sellerId, Pageable pageable) {
        // 1. 판매자의 경매 목록 조회
        Page<com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem> auctionPage =
                auctionSupport.findBySellerIdPaged(sellerId, pageable);
        
        // 2. DTO 변환 후 반환
        return auctionPage.map(AuctionItemResponse::from);
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViewCount(Long auctionId) {
        // 1. 경매 조회
        com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem =
                auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 조회수 증가 (엔티티 메서드 호출)
        auctionItem.increaseViewCount();
        
        // 3. 저장 (dirty checking으로 자동 저장됨)
        // TODO: Redis 캐싱은 나중에 추가
    }
    
    /**
     * 종료 시간이 지난 경매 목록 조회 (스케줄러용)
     */
    public java.util.List<com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem> 
            findExpiredAuctions(java.time.LocalDateTime now) {
        return auctionSupport.findExpiredAuctions(now);
    }

}
