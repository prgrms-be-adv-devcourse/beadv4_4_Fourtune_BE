package com.fourtune.shared.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionItemResponse(
    Long id,
    Long sellerId,
    String sellerNickname,
    String title,
    String category,
    BigDecimal startPrice,
    BigDecimal currentPrice,
    BigDecimal buyNowPrice,
    Boolean buyNowEnabled,
    Boolean buyNowDisabledByPolicy,
    String status,
    LocalDateTime auctionEndTime,
    Long viewCount,
    Integer watchlistCount,
    Integer bidCount,
    String thumbnailUrl
) {

    /**
     * 조회수만 바꾼 복사 (Redis 합산 값 반영용)
     */
    public AuctionItemResponse withViewCount(long viewCount) {
        return new AuctionItemResponse(
                id, sellerId, sellerNickname, title, category,
                startPrice, currentPrice, buyNowPrice, buyNowEnabled, buyNowDisabledByPolicy,
                status, auctionEndTime, viewCount, watchlistCount, bidCount, thumbnailUrl
        );
    }
}
