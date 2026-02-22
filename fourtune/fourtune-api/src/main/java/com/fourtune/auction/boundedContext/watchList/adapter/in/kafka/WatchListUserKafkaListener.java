package com.fourtune.auction.boundedContext.watchList.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.shared.user.event.UserEventType;
import com.fourtune.auction.boundedContext.watchList.application.service.WatchListService;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * WatchList 도메인의 User 이벤트 Kafka Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.user-events.enabled", havingValue = "true", matchIfMissing = false)
public class WatchListUserKafkaListener {

    private final WatchListService watchListService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.USER_EVENTS_TOPIC,
            groupId = "watchlist-user-events-group",
            containerFactory = "userEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleUserEvent(String payload,
                                @Header("X-Event-Type") String eventType,
                                Acknowledgment ack) {
        try {
            log.info("[WatchList] User 이벤트 수신: type={}", eventType);

            UserResponse user = objectMapper.readValue(payload, UserResponse.class);
            UserEventType type = UserEventType.valueOf(eventType);

            switch (type) {
                case USER_JOINED, USER_MODIFIED -> watchListService.syncUser(user);
                case USER_DELETED -> watchListService.syncUser(user);
            }

            ack.acknowledge();
            log.debug("[WatchList] User 이벤트 처리 완료: type={}, userId={}", eventType, user.id());

        } catch (Exception e) {
            log.error("[WatchList] User 이벤트 처리 실패: type={}, error={}", eventType, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
