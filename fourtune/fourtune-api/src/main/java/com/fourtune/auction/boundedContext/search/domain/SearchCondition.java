package com.fourtune.auction.boundedContext.search.domain;

import com.fourtune.auction.boundedContext.search.domain.constant.SearchSort;

import java.util.Set;

public record SearchCondition(
        String keyword,
        Set<String> categories,     // enum name 문자열들
        SearchPriceRange searchPriceRange,
        Set<String> statuses,       // "SCHEDULED", "ACTIVE", "ENDED" 등
        SearchSort sort,
        int page                    // 1부터 받을 예정
) {
    public int safePage() { return Math.max(1, page); }
}
