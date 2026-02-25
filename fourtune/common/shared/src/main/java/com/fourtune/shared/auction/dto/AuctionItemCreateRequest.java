package com.fourtune.shared.auction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionItemCreateRequest(
        @NotBlank String title,
        String description,
        @NotNull String category,
        @NotNull @Min(1000) BigDecimal startPrice,
        @Min(1000) Integer bidUnit,
        BigDecimal buyNowPrice,
        @NotNull LocalDateTime auctionStartTime,
        @NotNull LocalDateTime auctionEndTime,
        List<String> imageUrls) {
    // TODO: sellerId 추가 메서드
}
