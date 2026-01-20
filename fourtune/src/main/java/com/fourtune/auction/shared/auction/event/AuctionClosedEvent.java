package com.fourtune.auction.shared.auction.event;

import java.math.BigDecimal;

public record AuctionClosedEvent(
    Long auctionId,
    Long winnerId,      // 낙찰자 ID (없으면 null)
    BigDecimal finalPrice,  // 낙찰가 (없으면 null)
    String orderId      // 주문번호 (없으면 null)
) {
}
