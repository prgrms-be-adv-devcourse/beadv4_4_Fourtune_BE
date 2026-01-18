package com.fourtune.auction.shared.auction.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입찰 완료 이벤트
 * - 경매 도메인에서 발행
 * - 실시간 알림, 자동 연장 등에 사용
 */
public record BidPlacedEvent(
    Long bidId,
    Long auctionId,
    Long bidderId,
    BigDecimal bidAmount,
    LocalDateTime bidTime
) {
    // TODO: 이벤트 핸들러 구현
    // - AuctionEventListener.handleBidPlaced()
    // - NotificationEventListener.handleBidPlaced() (알림 전송)
}
