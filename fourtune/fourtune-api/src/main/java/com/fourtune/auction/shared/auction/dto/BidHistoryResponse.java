package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;

import java.util.List;
import java.util.Map;

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
        return from(auctionItem, bids, null);
    }

    /**
     * AuctionItem, Bid 목록, bidderId별 닉네임 맵으로 BidHistoryResponse 생성
     */
    public static BidHistoryResponse from(AuctionItem auctionItem, List<Bid> bids,
                                          Map<Long, String> bidderNicknames) {
        List<BidResponse> bidResponses = bids.stream()
                .map(bid -> BidResponse.from(bid,
                        bidderNicknames != null ? bidderNicknames.get(bid.getBidderId()) : null))
                .toList();
        return new BidHistoryResponse(
                auctionItem.getId(),
                auctionItem.getTitle(),
                auctionItem.getBidCount(),
                bidResponses
        );
    }
}
