package com.fourtune.auction.boundedContext.watchList.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.watchList.application.service.WatchListService;
import com.fourtune.common.global.config.kafka.KafkaTopicConfig;
import com.fourtune.common.shared.auction.event.AuctionClosedEvent;
import com.fourtune.common.shared.auction.event.AuctionItemCreatedEvent;
import com.fourtune.common.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.common.shared.auction.event.AuctionStartedEvent;
import com.fourtune.common.shared.auction.kafka.AuctionEventMapper;
import com.fourtune.common.shared.auction.kafka.AuctionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * WatchList 도메인의 Auction 이벤트 Kafka Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.auction-events.enabled", havingValue = "true", matchIfMissing = false)
public class WatchListAuctionKafkaListener {

    private final WatchListService watchListService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.AUCTION_EVENTS_TOPIC,
            groupId = "watchlist-auction-events-group",
            containerFactory = "auctionEventKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null) {
            return;
        }

        try {
            AuctionEventType type = AuctionEventType.valueOf(eventType);

            switch (type) {
                case AUCTION_ITEM_CREATED -> {
                    AuctionItemCreatedEvent event = objectMapper.readValue(
                            payload, AuctionItemCreatedEvent.class);
                    watchListService.syncAuctionItem(
                            event.auctionItemId(), event.title(), event.currentPrice(), event.thumbnailUrl());
                    log.debug("[WatchList] AuctionItemCreated 처리 완료: auctionItemId={}", event.auctionItemId());
                }
                case AUCTION_ITEM_UPDATED -> {
                    AuctionItemUpdatedEvent event = objectMapper.readValue(
                            payload, AuctionItemUpdatedEvent.class);
                    watchListService.syncAuctionItem(
                            event.auctionItemId(), event.title(), event.currentPrice(), event.thumbnailUrl());
                    log.debug("[WatchList] AuctionItemUpdated 처리 완료: auctionItemId={}", event.auctionItemId());
                }
                case AUCTION_STARTED -> {
                    AuctionStartedEvent event = objectMapper.readValue(
                            payload, AuctionStartedEvent.class);
                    watchListService.processAuctionStart(event.auctionId());
                    log.debug("[WatchList] AuctionStarted 처리 완료: auctionId={}", event.auctionId());
                }
                case AUCTION_CLOSED -> {
                    AuctionClosedEvent event = objectMapper.readValue(
                            payload, AuctionClosedEvent.class);
                    watchListService.processAuctionEnd(event.auctionId());
                    log.debug("[WatchList] AuctionClosed 처리 완료: auctionId={}", event.auctionId());
                }
                default -> {
                    // 관심상품 도메인에서 처리하지 않는 이벤트는 무시
                }
            }
        } catch (IllegalArgumentException e) {
            log.warn("[WatchList] 알 수 없는 이벤트 타입: {}", eventType);
        } catch (Exception e) {
            log.error("[WatchList] Auction 이벤트 처리 실패: eventType={}, error={}", eventType, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
