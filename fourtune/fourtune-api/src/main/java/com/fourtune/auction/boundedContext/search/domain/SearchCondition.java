package com.fourtune.auction.boundedContext.search.domain;

import com.fourtune.auction.boundedContext.search.domain.constant.SearchSort;

import java.util.Set;

public record SearchCondition(
        String keyword,
        Set<String> categories, // enum name 문자열들
        SearchPriceRange searchPriceRange,
        Set<String> statuses, // "SCHEDULED", "ACTIVE", "ENDED" 등
        SearchSort sort,
        int page, // 1부터 받을 예정
        Integer size // 페이징 사이즈를 직접 지정할 경우 (null이면 기본 프로퍼티 사용)
) {
    // 하위 호환성 (테스트 등에 사용됨)
    public SearchCondition(String keyword, Set<String> categories, SearchPriceRange searchPriceRange,
            Set<String> statuses, SearchSort sort, int page) {
        this(keyword, categories, searchPriceRange, statuses, sort, page, null);
    }

    public int safePage() {
        return Math.max(1, page);
    }
}
