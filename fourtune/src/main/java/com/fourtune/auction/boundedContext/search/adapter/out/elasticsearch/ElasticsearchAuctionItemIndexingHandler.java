package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch;

import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.document.SearchAuctionItemDocument;
import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.repository.SearchAuctionItemCrudRepository;
import com.fourtune.auction.boundedContext.search.application.service.AuctionItemIndexingHandler;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// 정본 변경을 ES에 반영
@Component
@RequiredArgsConstructor
public class ElasticsearchAuctionItemIndexingHandler implements AuctionItemIndexingHandler {

    private final SearchAuctionItemCrudRepository crudRepository;

    @Override
    public void upsert(SearchAuctionItemView view) {
        if (view == null) return;
        if (view.auctionItemId() == null) return;

        SearchAuctionItemDocument doc = toDocument(view);
        crudRepository.save(doc); // ✅ upsert
    }

    @Override
    public void delete(Long auctionItemId) {
        if (auctionItemId == null) return;
        crudRepository.deleteById(auctionItemId);
    }

    private SearchAuctionItemDocument toDocument(SearchAuctionItemView v) {
        // currentPrice null 방어: 가격필터 누락 방지
        // 예정(SCHEDULED) 경매에서는 아직 입찰이 없어서 currentPrice가 null일 수 있음
        BigDecimal currentPrice = (v.currentPrice() != null) ? v.currentPrice() : v.startPrice();

        return SearchAuctionItemDocument.builder()
                .auctionItemId(v.auctionItemId())
                .title(v.title())
                .description(v.description())
                .category(v.category())
                .status(v.status())
                .startPrice(v.startPrice())
                .currentPrice(currentPrice)
                .buyNowPrice(v.buyNowPrice())
                .buyNowEnabled(v.buyNowEnabled())
                .startAt(v.startAt())
                .endAt(v.endAt())
                .thumbnailUrl(v.thumbnailUrl())
                .createdAt(v.createdAt())
                .updatedAt(v.updatedAt())
                .viewCount(v.viewCount())
                .watchlistCount(v.watchlistCount())
                .bidCount(v.bidCount())
                .build();
    }
}
