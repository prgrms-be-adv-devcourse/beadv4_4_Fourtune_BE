package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.document.SearchAuctionItemDocument;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchFacade {
    private final SearchQueryUseCase queryUseCase;

    public SearchResultPage<SearchAuctionItemView> search(SearchCondition condition) {
        return queryUseCase.search(condition);
    }
}
