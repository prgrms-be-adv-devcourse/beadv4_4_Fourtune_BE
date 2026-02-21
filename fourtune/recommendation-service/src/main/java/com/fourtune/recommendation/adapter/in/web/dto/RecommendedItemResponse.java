package com.fourtune.recommendation.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 추천 상품 응답 DTO.
 * Feign 응답(SearchAuctionItemView)을 클라이언트에 전달하기 위한 record 입니다.
 */
public record RecommendedItemResponse(
        Long auctionItemId,
        String title,
        String category,
        String status,
        BigDecimal currentPrice,
        BigDecimal buyNowPrice,
        Boolean buyNowEnabled,
        String thumbnailUrl,
        LocalDateTime startAt,
        LocalDateTime endAt,
        long viewCount,
        int watchlistCount,
        int bidCount) {
}
