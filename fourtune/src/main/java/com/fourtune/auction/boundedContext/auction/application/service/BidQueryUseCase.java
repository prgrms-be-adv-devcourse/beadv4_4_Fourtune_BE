package com.fourtune.auction.boundedContext.auction.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 입찰 조회 UseCase
 * - 경매별 입찰 내역 조회
 * - 사용자별 입찰 내역 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidQueryUseCase {

    private final BidSupport bidSupport;
    private final AuctionSupport auctionSupport;

    /**
     * 경매의 입찰 내역 조회
     */
    public List<Object> getAuctionBids(Long auctionId) {
        // TODO: 구현 필요
        // 1. 경매 존재 확인
        // 2. 입찰 내역 조회 (최신순)
        // 3. DTO 변환 후 반환
        return null;
    }

    /**
     * 사용자의 입찰 내역 조회
     */
    public List<Object> getUserBids(Long bidderId) {
        // TODO: 구현 필요
        // 1. 사용자의 입찰 내역 조회
        // 2. DTO 변환 후 반환 (경매 정보 포함)
        return null;
    }

    /**
     * 경매의 최고가 입찰 조회
     */
    public Object getHighestBid(Long auctionId) {
        // TODO: 구현 필요
        return null;
    }

}
