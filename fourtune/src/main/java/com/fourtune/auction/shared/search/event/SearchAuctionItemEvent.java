package com.fourtune.auction.shared.search.event;

import java.time.LocalDateTime;

/**
 * 경매 검색 이벤트
 * - 검색 도메인에서 발행
 * - 추천 데이터 수집(검색어 로그) 등에 사용
 */
public record SearchAuctionItemEvent(
        Long userId, // 비로그인 시 null 가능
        String keyword,
        LocalDateTime searchedAt) {
}
