package com.fourtune.auction.shared.auction.event;

import java.time.LocalDateTime;

public record AuctionExtendedEvent(
    Long auctionId,
    LocalDateTime newEndTime
) {
}
