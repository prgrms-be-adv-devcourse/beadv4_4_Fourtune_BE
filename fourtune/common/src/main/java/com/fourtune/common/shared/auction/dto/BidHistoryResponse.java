package com.fourtune.common.shared.auction.dto;

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

}
