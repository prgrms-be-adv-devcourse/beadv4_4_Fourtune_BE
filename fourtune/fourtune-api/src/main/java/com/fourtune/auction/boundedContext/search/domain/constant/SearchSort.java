package com.fourtune.auction.boundedContext.search.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum SearchSort {
    LATEST("최신순", List.of(
            new SortSpec("createdAt", SortOrder.DESC)
    )),

    POPULAR("인기순", List.of(
            new SortSpec("viewCount", SortOrder.DESC)
            /*TODO: 인기순 가중치 고도화 규칙 세우기*/
//            new SortSpec("bidCount", SortOrder.DESC),
//            new SortSpec("watchListCount", SortOrder.DESC)
    )),

    ENDS_SOON("마감임박순", List.of(
            new SortSpec("endAt", SortOrder.ASC) // 종료 시간 오름차순 (빠른 시간부터)
    ));

    private final String description;
    private final List<SortSpec> sortSpecs;

    public record SortSpec(String field, SortOrder order) {}
}

