package com.fourtune.shared.auction.event;

public record AuctionCreatedEvent(
    Long auctionId,
    Long sellerId,
    String category
) {
    public AuctionCreatedEvent(Long auctionId) {
        this(auctionId, null, null);
    }
}
