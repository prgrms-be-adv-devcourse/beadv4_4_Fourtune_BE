package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.*;

public interface AuctionItemSearchEngine {
    SearchResultPage<SearchAuctionItemView> search(SearchCondition condition);
}