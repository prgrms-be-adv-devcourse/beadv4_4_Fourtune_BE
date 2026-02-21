package com.fourtune.shared.auction.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매 아이템 생성 이벤트 (Search 인덱싱 전용)
 * - 검색 인덱싱에 필요한 모든 필드를 포함한 스냅샷
 * - Search 도메인에서 Auction DB 직접 조회 없이 인덱싱 가능
 */
public record AuctionItemCreatedEvent(
    Long auctionItemId,
    Long sellerId,
    String sellerName,
    String title,
    String description,
    String category,
    String status,
    BigDecimal startPrice,
    BigDecimal currentPrice,
    BigDecimal buyNowPrice,
    Boolean buyNowEnabled,
    LocalDateTime startAt,
    LocalDateTime endAt,
    String thumbnailUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long viewCount,
    Integer bidCount,
    Integer watchlistCount
) {
}
