package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.repository;

import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.document.SearchAuctionItemDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

@Profile("!test")
public interface SearchAuctionItemCrudRepository extends ElasticsearchRepository<SearchAuctionItemDocument, Long> {
}
