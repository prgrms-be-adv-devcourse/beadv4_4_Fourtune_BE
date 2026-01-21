package com.fourtune.auction.shared.auction.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매 시작 이벤트
 * - 경매 도메인에서 발행
 * - 관심상품 경매 시작 알림 등에 사용
 */
public record AuctionStartedEvent(
    Long auctionId,
    String auctionTitle,
    Long sellerId,
    BigDecimal startPrice,
    LocalDateTime auctionEndTime
) {
}
