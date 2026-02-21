package com.fourtune.shared.auction.event;

import java.time.LocalDateTime;

public record AuctionExtendedEvent(
    Long auctionId,
    LocalDateTime newEndTime
) {
}
