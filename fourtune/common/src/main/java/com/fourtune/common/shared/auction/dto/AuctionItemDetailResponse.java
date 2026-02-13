package com.fourtune.common.shared.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionItemDetailResponse(
    Long id,
    Long sellerId,
    String sellerNickname,
    String title,
    String description,
    String category,
    BigDecimal startPrice,
    BigDecimal currentPrice,
    Integer bidUnit,
    BigDecimal buyNowPrice,
    Boolean buyNowEnabled,
    Boolean buyNowDisabledByPolicy,
    String status,
    LocalDateTime auctionStartTime,
    LocalDateTime auctionEndTime,
    Long viewCount,
    Integer watchlistCount,
    Integer bidCount,
    List<String> imageUrls
) {

    /**
     * 조회수만 바꾼 복사 (Redis 합산 값 반영용)
     */
    public AuctionItemDetailResponse withViewCount(long viewCount) {
        return new AuctionItemDetailResponse(
                id, sellerId, sellerNickname, title, description, category,
                startPrice, currentPrice, bidUnit, buyNowPrice, buyNowEnabled, buyNowDisabledByPolicy,
                status, auctionStartTime, auctionEndTime, viewCount, watchlistCount, bidCount, imageUrls
        );
    }
}
