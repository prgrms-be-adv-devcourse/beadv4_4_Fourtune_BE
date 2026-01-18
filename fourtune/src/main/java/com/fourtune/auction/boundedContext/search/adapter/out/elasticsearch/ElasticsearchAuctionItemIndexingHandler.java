package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch;

import com.fourtune.auction.boundedContext.search.application.service.AuctionItemIndexingHandler;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class ElasticsearchAuctionItemIndexingHandler implements AuctionItemIndexingHandler {

    private final ElasticsearchTemplate template;

    @Override
    public void upsert(SearchAuctionItemView view) {
        // TODO: ES save/upsert 구현
    }

    @Override
    public void delete(Long auctionItemId) {
        // TODO: ES delete 구현
    }
}
