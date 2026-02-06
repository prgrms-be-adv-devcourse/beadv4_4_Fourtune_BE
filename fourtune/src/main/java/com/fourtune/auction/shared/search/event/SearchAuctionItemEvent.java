package com.fourtune.auction.shared.search.event;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 * 경매 검색 이벤트
 * - 검색 도메인에서 발행
 * - 추천 데이터 수집(검색어 로그) 등에 사용
 */
public record SearchAuctionItemEvent(
        Long userId,
        String keyword,
        List<String> categories,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<String> status,
        Integer resultCount,
        Boolean isSuccess,
        LocalDateTime searchedAt) {
}
