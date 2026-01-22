package com.fourtune.auction.shared.auction.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입찰 취소 이벤트
 * - 경매 도메인에서 발행
 * - 입찰 취소 알림 등에 사용
 */
public record BidCanceledEvent(
    Long bidId,
    Long auctionId,
    String auctionTitle,
    Long sellerId,
    Long bidderId,
    BigDecimal bidAmount,
    LocalDateTime canceledTime
) {
}
