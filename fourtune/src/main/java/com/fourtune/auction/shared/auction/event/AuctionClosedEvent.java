package com.fourtune.auction.shared.auction.event;

public record AuctionClosedEvent(
    Long auctionId,
    Long winnerId,
    Long sellerId
) {
}
