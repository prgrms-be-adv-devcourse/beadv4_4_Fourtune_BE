package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;

public interface AuctionItemIndexingHandler {
    void upsert(SearchAuctionItemView view);
    void delete(Long auctionItemId);
}
