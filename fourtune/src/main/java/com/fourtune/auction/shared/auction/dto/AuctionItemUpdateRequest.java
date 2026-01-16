package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionItemUpdateRequest(
    String title,
    String description,
    Category category,
    BigDecimal startPrice,
    Integer bidUnit,
    BigDecimal buyNowPrice,
    LocalDateTime auctionStartTime,
    LocalDateTime auctionEndTime
) {
}
