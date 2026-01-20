package com.fourtune.auction.shared.auction.dto;

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
    // TODO: from(AuctionItem, List<Bid>) 정적 팩토리 메서드 구현
}
