package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionItemResponse(
    Long id,
    Long sellerId,
    String title,
    Category category,
    BigDecimal startPrice,
    BigDecimal currentPrice,
    AuctionStatus status,
    LocalDateTime auctionEndTime,
    Long viewCount,
    Integer bidCount,
    String thumbnailUrl
) {
    // TODO: from() 메서드 구현
}
