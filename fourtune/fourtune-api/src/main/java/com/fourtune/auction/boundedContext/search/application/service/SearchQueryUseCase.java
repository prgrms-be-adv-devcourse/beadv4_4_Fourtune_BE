package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.SearchPriceRange;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.common.global.error.ErrorCode;
import com.fourtune.common.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SearchQueryUseCase {

    private final AuctionItemSearchEngine searchEngine;

    public SearchResultPage<SearchAuctionItemView> search(SearchCondition condition) {

        // 가격 범위 가드
        SearchPriceRange pr = condition.searchPriceRange();
        if (pr != null && !pr.isEmpty()) {
            BigDecimal min = pr.min();
            BigDecimal max = pr.max();
            if (min != null && max != null && min.compareTo(max) > 0) {
                throw new BusinessException(ErrorCode.SEARCH_INVALID_CONDITION);
            }
        }

        return searchEngine.search(condition);
    }
}
