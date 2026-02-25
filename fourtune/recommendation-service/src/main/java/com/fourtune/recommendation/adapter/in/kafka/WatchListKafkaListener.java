package com.fourtune.recommendation.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.recommendation.application.service.UserPreferenceService;
import com.fourtune.recommendation.common.RecommendationConstants;
import com.fourtune.shared.watchList.event.WatchListToggleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 찜하기 토글 이벤트 Consumer
 * watchlist-events 토픽에서 WATCHLIST_ITEM_ADDED / WATCHLIST_ITEM_REMOVED 이벤트를 소비하여
 * 찜한 카테고리를 프로파일에 반영
 * ADD → 가중치 +3
 * REMOVE → 가중치 -3
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true")
public class WatchListKafkaListener {

    private static final String WATCHLIST_ITEM_ADDED = "WATCHLIST_ITEM_ADDED";
    private static final String WATCHLIST_ITEM_REMOVED = "WATCHLIST_ITEM_REMOVED";

    private final ObjectMapper objectMapper;
    private final UserPreferenceService userPreferenceService;

    @KafkaListener(topics = KafkaTopicConfig.WATCHLIST_EVENTS_TOPIC, groupId = "recommendation-watchlist-group", containerFactory = "watchlistEventKafkaListenerContainerFactory")
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (!WATCHLIST_ITEM_ADDED.equals(eventType) && !WATCHLIST_ITEM_REMOVED.equals(eventType)) {
            // WATCHLIST_AUCTION_STARTED, WATCHLIST_AUCTION_ENDED 등은 무시
            return;
        }

        try {
            WatchListToggleEvent event = objectMapper.readValue(payload, WatchListToggleEvent.class);

            Long userId = event.userId();
            String action = event.action();
            String category = event.itemData().category();

            if (userId == null || action == null || category == null) {
                log.warn("[REC][WATCHLIST] 불완전한 이벤트 수신: {}", payload);
                return;
            }

            int weight = "ADD".equals(action) ? RecommendationConstants.WATCHLIST_ADD_WEIGHT
                    : RecommendationConstants.WATCHLIST_REMOVE_WEIGHT;
            userPreferenceService.incrementCategoryScore(userId, category, weight);

            log.info("[REC][WATCHLIST] 프로파일 반영: userId={}, action={}, category={}, weight={}",
                    userId, action, category, weight);

        } catch (Exception e) {
            log.error("[REC][WATCHLIST] 이벤트 처리 실패: eventType={}, error={}", eventType, e.getMessage(), e);
        }
    }
}
