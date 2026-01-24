package com.fourtune.auction.boundedContext.search.domain;

import java.math.BigDecimal;

public record SearchPriceRange(BigDecimal min, BigDecimal max) {
    public boolean isEmpty() { return min == null && max == null; }
}
