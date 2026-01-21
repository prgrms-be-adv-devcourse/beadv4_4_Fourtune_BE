package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;

import java.util.List;

/**
 * 입찰 내역 응답 DTO (경매별)
 */
public record BidHistoryResponse(
    Long auctionId,
    String auctionTitle,
    Integer totalBidCount,
    List<BidResponse> bids
) {
    /**
     * AuctionItem과 Bid 목록으로 BidHistoryResponse 생성
     */
    public static BidHistoryResponse from(AuctionItem auctionItem, List<Bid> bids) {
        List<BidResponse> bidResponses = bids.stream()
                .map(BidResponse::from)
                .toList();
        
        return new BidHistoryResponse(
                auctionItem.getId(),
                auctionItem.getTitle(),
                auctionItem.getBidCount(),
                bidResponses
        );
    }
}
