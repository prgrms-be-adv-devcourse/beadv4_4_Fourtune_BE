package com.fourtune.common.shared.auction.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 완료 이벤트
 * - 주문 결제 완료 시 발행
 * - 정산, 알림 등에 사용
 */
public record OrderCompletedEvent(
    String orderId,           // 주문 ID (UUID)
    Long auctionId,           // 경매 ID
    Long winnerId,            // 구매자 ID
    Long sellerId,            // 판매자 ID
    BigDecimal amount,        // 결제 금액
    String orderName,         // 주문명
    LocalDateTime paidAt      // 결제 완료 시간
) {
}
