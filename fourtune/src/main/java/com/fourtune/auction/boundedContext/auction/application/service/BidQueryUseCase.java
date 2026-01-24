package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.shared.auction.dto.BidDetailResponse;
import com.fourtune.auction.shared.auction.dto.BidHistoryResponse;
import com.fourtune.auction.shared.auction.dto.BidResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    public BidHistoryResponse getAuctionBids(Long auctionId) {
        // 1. 경매 존재 확인
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 입찰 내역 조회 (입찰가 높은순)
        List<Bid> bids = bidSupport.findByAuctionId(auctionId);
        
        // 3. DTO 변환 후 반환
        return BidHistoryResponse.from(auctionItem, bids);
    }

    /**
     * 사용자의 입찰 내역 조회
     */
    public List<BidResponse> getUserBids(Long bidderId) {
        // 1. 사용자의 입찰 내역 조회
        List<Bid> bids = bidSupport.findByBidderId(bidderId);
        
        // 2. DTO 변환 후 반환
        return bids.stream()
                .map(BidResponse::from)
                .toList();
    }

    /**
     * 경매의 최고가 입찰 조회
     */
    public BidResponse getHighestBid(Long auctionId) {
        // 1. 경매 존재 확인
        auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 최고가 입찰 조회
        Optional<Bid> highestBid = bidSupport.findHighestBid(auctionId);
        
        // 3. DTO 변환 후 반환 (없으면 null)
        return highestBid.map(BidResponse::from).orElse(null);
    }

    /**
     * 입찰 ID로 상세 조회
     */
    public BidDetailResponse getBidDetail(Long bidId) {
        // 1. 입찰 조회
        Bid bid = bidSupport.findByIdOrThrow(bidId);
        
        // 2. 경매 정보 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(bid.getAuctionId());
        
        // 3. DTO 변환 후 반환
        return BidDetailResponse.from(bid, auctionItem);
    }

}
