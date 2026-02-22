package com.fourtune.recommendation.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.recommendation.application.service.UserPreferenceService;
import com.fourtune.recommendation.common.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 찜하기 토글 이벤트 Consumer (골격)
 * watchlist-events 토픽에서 WATCHLIST_TOGGLED 이벤트를 필터링하여
 * 찜한 카테고리를 프로파일에 반영
 * ADD → 가중치 +3
 * REMOVE → 가중치 -3
 *
 * TODO: WatchListToggledEvent 신규 생성 및 Producer 구현 후 활성화
 * (현재 findAllByAuction 토글 시 Kafka 이벤트 미발행)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true")
public class WatchListKafkaListener {

    private static final String WATCHLIST_TOGGLED = "WATCHLIST_TOGGLED";

    private final ObjectMapper objectMapper;
    private final UserPreferenceService userPreferenceService;

    @KafkaListener(topics = KafkaTopicConfig.WATCHLIST_EVENTS_TOPIC, groupId = "recommendation-watchlist-group")
    @SuppressWarnings("unchecked")
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (!WATCHLIST_TOGGLED.equals(eventType)) {
            // WATCHLIST_AUCTION_STARTED, WATCHLIST_AUCTION_ENDED 등은 무시
            return;
        }

        try {
            // TODO: WatchListToggledEvent DTO가 common:shared에 생성된 후 타입 안전 파싱으로 교체
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);

            Long userId = toLong(event.get("userId"));
            String action = (String) event.get("action");
            String category = (String) event.get("category");

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

    private Long toLong(Object value) {
        if (value instanceof Number num) {
            return num.longValue();
        }
        return null;
    }
}
