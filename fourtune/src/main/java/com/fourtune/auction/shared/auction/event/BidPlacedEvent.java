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
    String auctionTitle,
    Long sellerId,
    Long bidderId,
    Long previousBidderId,  // 이전 최고 입찰자 (알림용)
    BigDecimal bidAmount,
    LocalDateTime bidTime
) {
}
