package com.fourtune.auction.boundedContext.search.adapter.in.event;

import com.fourtune.auction.boundedContext.search.application.service.AuctionItemIndexingHandler;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.shared.auction.event.AuctionItemCreatedEvent;
import com.fourtune.auction.shared.auction.event.AuctionItemDeletedEvent;
import com.fourtune.auction.shared.auction.event.AuctionItemUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * AuctionItem 이벤트를 수신하여 ElasticSearch 인덱스를 업데이트하는 리스너
 * - AuctionItemCreatedEvent: 경매 생성 시 인덱스에 추가
 * - AuctionItemUpdatedEvent: 경매 수정 시 인덱스 업데이트
 * - AuctionItemDeletedEvent: 경매 삭제 시 인덱스에서 제거
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionItemIndexEventListener {

    private final AuctionItemIndexingHandler indexingHandler;

    /**
     * 경매 생성 이벤트 처리
     */
    @EventListener
    public void handleCreated(AuctionItemCreatedEvent event) {
        log.info("[SEARCH][INDEX] Received AuctionItemCreatedEvent: auctionItemId={}", event.auctionItemId());
        try {
            SearchAuctionItemView view = toView(event);
            indexingHandler.upsert(view);
            log.debug("[SEARCH][INDEX] Successfully indexed auction item: {}", event.auctionItemId());
        } catch (Exception e) {
            log.error("[SEARCH][INDEX] Failed to index created auction item: {}", event.auctionItemId(), e);
        }
    }

    /**
     * 경매 수정 이벤트 처리
     */
    @EventListener
    public void handleUpdated(AuctionItemUpdatedEvent event) {
        log.info("[SEARCH][INDEX] Received AuctionItemUpdatedEvent: auctionItemId={}", event.auctionItemId());
        try {
            SearchAuctionItemView view = toView(event);
            indexingHandler.upsert(view);
            log.debug("[SEARCH][INDEX] Successfully updated auction item index: {}", event.auctionItemId());
        } catch (Exception e) {
            log.error("[SEARCH][INDEX] Failed to update auction item index: {}", event.auctionItemId(), e);
        }
    }

    /**
     * 경매 삭제 이벤트 처리
     */
    @EventListener
    public void handleDeleted(AuctionItemDeletedEvent event) {
        log.info("[SEARCH][INDEX] Received AuctionItemDeletedEvent: auctionItemId={}", event.auctionItemId());
        try {
            indexingHandler.delete(event.auctionItemId());
            log.debug("[SEARCH][INDEX] Successfully deleted auction item from index: {}", event.auctionItemId());
        } catch (Exception e) {
            log.error("[SEARCH][INDEX] Failed to delete auction item from index: {}", event.auctionItemId(), e);
        }
    }

    /**
     * AuctionItemCreatedEvent를 SearchAuctionItemView로 변환
     */
    private SearchAuctionItemView toView(AuctionItemCreatedEvent event) {
        return new SearchAuctionItemView(
                event.auctionItemId(),
                event.title(),
                event.description(),
                event.category().name(),
                event.status().name(),
                event.startPrice(),
                event.currentPrice(),
                event.startAt(),
                event.endAt(),
                event.thumbnailUrl(),
                event.createdAt(),
                event.updatedAt(),
                event.viewCount(),
                event.watchlistCount(),
                event.bidCount());
    }

    /**
     * AuctionItemUpdatedEvent를 SearchAuctionItemView로 변환
     */
    private SearchAuctionItemView toView(AuctionItemUpdatedEvent event) {
        return new SearchAuctionItemView(
                event.auctionItemId(),
                event.title(),
                event.description(),
                event.category().name(),
                event.status().name(),
                event.startPrice(),
                event.currentPrice(),
                event.startAt(),
                event.endAt(),
                event.thumbnailUrl(),
                event.createdAt(),
                event.updatedAt(),
                event.viewCount(),
                event.watchlistCount(),
                event.bidCount());
    }
}
