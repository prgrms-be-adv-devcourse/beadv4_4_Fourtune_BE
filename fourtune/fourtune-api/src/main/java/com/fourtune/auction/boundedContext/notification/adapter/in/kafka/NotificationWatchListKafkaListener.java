package com.fourtune.auction.boundedContext.notification.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification 도메인의 WatchList 이벤트 Kafka Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.watchlist-events.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationWatchListKafkaListener {

    private final NotificationFacade notificationFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.WATCHLIST_EVENTS_TOPIC,
            groupId = "notification-watchlist-events-group",
            containerFactory = "watchlistEventKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null) {
            return;
        }

        try {
            WatchListEventType type = WatchListEventType.valueOf(eventType);

            // 알림 처리 대상 이벤트만 파싱
            if (type != WatchListEventType.WATCHLIST_AUCTION_STARTED
                    && type != WatchListEventType.WATCHLIST_AUCTION_ENDED) {
                return;
            }

            JsonNode node = objectMapper.readTree(payload);
            Long auctionItemId = node.get("auctionItemId").asLong();
            String auctionTitle = node.has("auctionTitle") ? node.get("auctionTitle").asText("") : "";
            List<Long> users = new ArrayList<>();
            node.get("users").forEach(u -> users.add(u.asLong()));

            switch (type) {
                case WATCHLIST_AUCTION_STARTED -> {
                    notificationFacade.createGroupNotification(users, auctionItemId, NotificationType.WATCHLIST_START, auctionTitle);
                    log.debug("[Notification] WatchList 경매시작 알림 처리 완료: auctionItemId={}, users={}",
                            auctionItemId, users.size());
                }
                case WATCHLIST_AUCTION_ENDED -> {
                    notificationFacade.createGroupNotification(users, auctionItemId, NotificationType.WATCHLIST_END, auctionTitle);
                    log.debug("[Notification] WatchList 경매종료 알림 처리 완료: auctionItemId={}, users={}",
                            auctionItemId, users.size());
                }
            }
        } catch (IllegalArgumentException e) {
            log.warn("[Notification] 알 수 없는 WatchList 이벤트 타입: {}", eventType);
        } catch (Exception e) {
            log.error("[Notification] WatchList 이벤트 처리 실패: eventType={}, error={}", eventType, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
