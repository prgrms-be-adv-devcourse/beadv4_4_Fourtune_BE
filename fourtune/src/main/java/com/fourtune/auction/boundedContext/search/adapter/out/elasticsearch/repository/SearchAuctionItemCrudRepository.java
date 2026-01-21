package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.repository;

import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.document.SearchAuctionItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchAuctionItemCrudRepository extends ElasticsearchRepository<SearchAuctionItemDocument, Long> {
}
