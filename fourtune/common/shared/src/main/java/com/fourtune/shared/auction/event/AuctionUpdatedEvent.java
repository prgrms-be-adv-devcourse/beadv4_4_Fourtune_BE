package com.fourtune.shared.auction.event;

import java.math.BigDecimal;

/**
 * 경매 수정 이벤트
 * - 경매 정보 수정 시 발행
 * - 검색 인덱스 업데이트에 사용
 */
public record AuctionUpdatedEvent(
    Long auctionId,
    Long sellerId,
    String sellerName,
    String title,
    String description,
    BigDecimal buyNowPrice,
    Boolean buyNowEnabled,
    String category
) {
}
