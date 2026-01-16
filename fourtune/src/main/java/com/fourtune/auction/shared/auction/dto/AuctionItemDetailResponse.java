package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionItemDetailResponse(
    Long id,
    Long sellerId,
    String title,
    String description,
    Category category,
    BigDecimal startPrice,
    BigDecimal currentPrice,
    Integer bidUnit,
    BigDecimal buyNowPrice,
    AuctionStatus status,
    LocalDateTime auctionStartTime,
    LocalDateTime auctionEndTime,
    Long viewCount,
    Integer watchlistCount,
    Integer bidCount,
    List<String> imageUrls
) {
    // TODO: from() 메서드 구현
}
