package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch;

import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import com.fourtune.auction.boundedContext.search.application.service.AuctionItemSearchEngine;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;

@Component
@RequiredArgsConstructor
public class ElasticsearchAuctionItemSearchEngine implements AuctionItemSearchEngine {

    private final ElasticsearchOperations operations;

    @Override
    public SearchResultPage<SearchAuctionItemView> search(SearchCondition condition) {
        // TODO: ES from/size 검색 구현
        // 이후 search_after로 교체 가능
        throw new UnsupportedOperationException("TODO");
    }
}