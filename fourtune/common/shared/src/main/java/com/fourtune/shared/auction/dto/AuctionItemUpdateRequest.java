package com.fourtune.shared.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionItemUpdateRequest(
    String title,
    String description,
    String category,
    BigDecimal startPrice,
    Integer bidUnit,
    BigDecimal buyNowPrice,
    LocalDateTime auctionStartTime,
    LocalDateTime auctionEndTime
) {
}
