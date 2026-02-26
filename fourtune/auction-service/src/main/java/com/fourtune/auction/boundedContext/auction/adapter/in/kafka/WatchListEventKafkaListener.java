package com.fourtune.auction.boundedContext.auction.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.auction.application.service.AuctionSupport;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.shared.watchList.event.WatchListToggleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * watchlist-events 소비 → AuctionItem.watchlistCount 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.auction-events.enabled", havingValue = "true", matchIfMissing = false)
public class WatchListEventKafkaListener {

    private final AuctionSupport auctionSupport;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(
            topics = KafkaTopicConfig.WATCHLIST_EVENTS_TOPIC,
            groupId = "auction-watchlist-events-group",
            containerFactory = "watchlistEventKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null) {
            return;
        }
        if (!eventType.equals("WATCHLIST_ITEM_ADDED") && !eventType.equals("WATCHLIST_ITEM_REMOVED")) {
            return;
        }

        try {
            WatchListToggleEvent event = objectMapper.readValue(payload, WatchListToggleEvent.class);
            Long auctionItemId = event.itemData().auctionId();

            AuctionItem item = auctionSupport.findById(auctionItemId).orElse(null);
            if (item == null) {
                log.warn("[WatchList] 경매 아이템 없음, watchlistCount 업데이트 스킵: auctionItemId={}", auctionItemId);
                return;
            }

            if ("WATCHLIST_ITEM_ADDED".equals(eventType)) {
                item.incrementWatchlistCount();
                log.debug("[WatchList] watchlistCount 증가: auctionItemId={}, count={}", auctionItemId, item.getWatchlistCount());
            } else {
                item.decrementWatchlistCount();
                log.debug("[WatchList] watchlistCount 감소: auctionItemId={}, count={}", auctionItemId, item.getWatchlistCount());
            }

            auctionSupport.save(item);
        } catch (Exception e) {
            log.error("[WatchList] watchlistCount 업데이트 실패: eventType={}, error={}", eventType, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
