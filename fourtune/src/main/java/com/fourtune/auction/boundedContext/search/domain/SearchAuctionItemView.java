package com.fourtune.auction.boundedContext.search.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SearchAuctionItemView(
        Long auctionItemId,
        String title,
        String description,
        String category,     // enum name
        String status,       // enum name
        BigDecimal startPrice,
        BigDecimal currentPrice,
        BigDecimal buyNowPrice,
        Boolean buyNowEnabled,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String thumbnailUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long viewCount,
        int watchlistCount,
        int bidCount
) {}
