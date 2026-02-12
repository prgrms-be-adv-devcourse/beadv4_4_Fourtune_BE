package com.fourtune.common.shared.auction.event;

import java.math.BigDecimal;

/**
 * 경매 종료 이벤트
 * - 경매 도메인에서 발행
 * - 알림, 정산 등에 사용
 */
public record AuctionClosedEvent(
    Long auctionId,
    String auctionTitle,
    Long sellerId,
    Long winnerId,          // 낙찰자 ID (없으면 null)
    BigDecimal finalPrice,  // 낙찰가 (없으면 null)
    String orderId          // 주문번호 (없으면 null)
) {
}
