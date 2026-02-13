package com.fourtune.auction.boundedContext.search.adapter.in.event;

import com.fourtune.auction.boundedContext.search.application.service.AuctionItemIndexingHandler;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.common.shared.auction.event.AuctionItemCreatedEvent;
import com.fourtune.common.shared.auction.event.AuctionItemDeletedEvent;
import com.fourtune.common.shared.auction.event.AuctionItemUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * AuctionItem 이벤트를 수신하여 ElasticSearch 인덱스를 업데이트하는 리스너
 *
 * 비동기 처리: @Async + @TransactionalEventListener(AFTER_COMMIT)
 * - 트랜잭션 커밋 후 별도 스레드에서 인덱싱하여 요청 응답을 블로킹하지 않음
 * - 커밋 후 실행으로 롤백된 데이터는 인덱싱되지 않음
 *
 * 향후 계획:
 * Kafka로 확장하여 이벤트 기반 아키텍처 구현
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
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
                event.category(),
                event.status(),
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
                event.bidCount(),
                event.sellerId(),
                event.sellerName());
    }

    // AuctionItemUpdatedEvent를 SearchAuctionItemView로 변환
    private SearchAuctionItemView toView(AuctionItemUpdatedEvent event) {
        return new SearchAuctionItemView(
                event.auctionItemId(),
                event.title(),
                event.description(),
                event.category(),
                event.status(),
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
                event.bidCount(),
                event.sellerId(),
                event.sellerName());
    }
}
