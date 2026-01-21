package com.fourtune.auction.boundedContext.search.domain;

import java.util.List;

public record SearchResultPage<T>(
        List<T> items,
        long totalElements,
        int page,
        int size,
        boolean hasNext
) {}