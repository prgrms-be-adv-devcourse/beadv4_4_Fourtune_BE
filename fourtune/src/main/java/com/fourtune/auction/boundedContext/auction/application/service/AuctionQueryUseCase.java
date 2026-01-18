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
     * 경매 상세 조회
     */
    public AuctionItemDetailResponse getAuctionDetail(Long auctionId) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 입찰 내역 조회 (최신 5개)
        // 3. 최고가 입찰 조회
        // 4. DTO 변환 후 반환
        return null;
    }

    /**
     * 경매 목록 조회 (페이징)
     */
    public Page<AuctionItemResponse> getAuctionList(Pageable pageable) {
        // TODO: 구현 필요
        // 1. 진행중인 경매 목록 조회
        // 2. DTO 변환 후 반환
        return null;
    }

    /**
     * 판매자의 경매 목록 조회
     */
    public Page<AuctionItemResponse> getSellerAuctions(Long sellerId, Pageable pageable) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViewCount(Long auctionId) {
        // TODO: 구현 필요
        // 1. Redis에서 조회수 증가
        // 2. 일정 주기로 DB 동기화 (Batch or Scheduler)
    }

}
