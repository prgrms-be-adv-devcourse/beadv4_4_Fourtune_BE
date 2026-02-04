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
 *
 * 현재: Spring Event 기반 동기 처리
 * 향후 계획:
 * Kafka로 확장하여 이벤트 기반 아키텍처 구현
 * 비동기 처리 (@Async + TransactionalEventListener)
 * 재시도 로직 (Spring Retry 또는 Kafka 재시도)
 * Dead Letter Queue 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionItemIndexEventListener {

    private final AuctionItemIndexingHandler indexingHandler;

    /**
     * 경매 생성 이벤트 처리
     * 
     * TODO: Kafka 확장 시
     * 1. @KafkaListener로 변경
     * 2. Topic: auction-item-created
     * 3. Consumer Group: search-indexing-group
     * 4. 재시도 정책 설정 (max attempts, backoff)
     * 5. DLQ 설정 (실패한 메시지 처리)
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
            // TODO: Kafka DLQ 또는 재시도 로직 추가
        }
    }

    /**
     * 경매 수정 이벤트 처리
     * 
     * TODO: Kafka 확장 시
     * - Topic: auction-item-updated
     * - 나머지는 handleCreated와 동일
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
            // TODO: Kafka DLQ 또는 재시도 로직 추가
        }
    }

    /**
     * 경매 삭제 이벤트 처리
     * 
     * TODO: Kafka 확장 시
     * - Topic: auction-item-deleted
     * - 나머지는 handleCreated와 동일
     */
    @EventListener
    public void handleDeleted(AuctionItemDeletedEvent event) {
        log.info("[SEARCH][INDEX] Received AuctionItemDeletedEvent: auctionItemId={}", event.auctionItemId());
        try {
            indexingHandler.delete(event.auctionItemId());
            log.debug("[SEARCH][INDEX] Successfully deleted auction item from index: {}", event.auctionItemId());
        } catch (Exception e) {
            log.error("[SEARCH][INDEX] Failed to delete auction item from index: {}", event.auctionItemId(), e);
            // TODO: Kafka DLQ 또는 재시도 로직 추가
        }
    }

    // AuctionItemCreatedEvent를 SearchAuctionItemView로 변환
    private SearchAuctionItemView toView(AuctionItemCreatedEvent event) {
        return new SearchAuctionItemView(
                event.auctionItemId(),
                event.title(),
                event.description(),
                event.category().name(),
                event.status().name(),
                event.startPrice(),
                event.currentPrice(),
                event.buyNowPrice(),
                event.buyNowEnabled(),
                event.startAt(),
                event.endAt(),
                event.thumbnailUrl(),
                event.createdAt(),
                event.updatedAt(),
                event.viewCount(),
                event.watchlistCount(),
                event.bidCount());
    }

    // AuctionItemUpdatedEvent를 SearchAuctionItemView로 변환
    private SearchAuctionItemView toView(AuctionItemUpdatedEvent event) {
        return new SearchAuctionItemView(
                event.auctionItemId(),
                event.title(),
                event.description(),
                event.category().name(),
                event.status().name(),
                event.startPrice(),
                event.currentPrice(),
                event.buyNowPrice(),
                event.buyNowEnabled(),
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
