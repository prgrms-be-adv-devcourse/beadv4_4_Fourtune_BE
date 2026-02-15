package com.fourtune.auction.boundedContext.search.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.search.application.service.AuctionItemIndexingHandler;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.common.global.config.kafka.KafkaTopicConfig;
import com.fourtune.common.shared.auction.event.AuctionItemCreatedEvent;
import com.fourtune.common.shared.auction.event.AuctionItemDeletedEvent;
import com.fourtune.common.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.common.shared.auction.kafka.AuctionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionItemSearchKafkaListener {

    private final AuctionItemIndexingHandler indexingHandler;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConfig.AUCTION_EVENTS_TOPIC, groupId = "search-auction-events-group", containerFactory = "auctionEventKafkaListenerContainerFactory")
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null) {
            return;
        }

        try {
            switch (AuctionEventType.valueOf(eventType)) {
                case AUCTION_ITEM_CREATED -> {
                    AuctionItemCreatedEvent event = objectMapper.readValue(payload, AuctionItemCreatedEvent.class);
                    indexingHandler.upsert(toView(event));
                    log.info("[SEARCH][KAFKA] Created event processed: auctionId={}", event.auctionItemId());
                }
                case AUCTION_ITEM_UPDATED -> {
                    AuctionItemUpdatedEvent event = objectMapper.readValue(payload, AuctionItemUpdatedEvent.class);
                    indexingHandler.upsert(toView(event));
                    log.info("[SEARCH][KAFKA] Updated event processed: auctionId={}", event.auctionItemId());
                }
                case AUCTION_ITEM_DELETED -> {
                    AuctionItemDeletedEvent event = objectMapper.readValue(payload, AuctionItemDeletedEvent.class);
                    indexingHandler.delete(event.auctionItemId());
                    log.info("[SEARCH][KAFKA] Deleted event processed: auctionId={}", event.auctionItemId());
                }
                default -> log.debug("Ignored event type for search: {}", eventType);
            }
        } catch (Exception e) {
            log.error("auction event 처리 failed: type={}, payload={}", eventType, payload, e);
            // ContainerFactory에 설정된 ErrorHandler가 재시도를 처리함
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
