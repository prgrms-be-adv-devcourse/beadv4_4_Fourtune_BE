package com.fourtune.auction.shared.auction.event;

import com.fourtune.auction.boundedContext.auction.domain.constant.Category;

public record AuctionCreatedEvent(
    Long auctionId,
    Long sellerId,
    Category category
) {
    public AuctionCreatedEvent(Long auctionId) {
        this(auctionId, null, null);
    }
}
