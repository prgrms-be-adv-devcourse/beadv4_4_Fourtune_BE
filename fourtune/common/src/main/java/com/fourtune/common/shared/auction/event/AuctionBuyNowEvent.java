package com.fourtune.common.shared.auction.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 즉시구매 완료 이벤트
 * - 경매 도메인에서 발행
 * - 장바구니 아이템 만료, 알림 등에 사용
 */
public record AuctionBuyNowEvent(
    Long auctionId,
    Long sellerId,
    Long buyerId,
    BigDecimal buyNowPrice,
    String orderId,
    LocalDateTime buyNowTime
) {
}
