package com.fourtune.recommendation.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.recommendation.application.service.UserPreferenceService;
import com.fourtune.recommendation.common.RecommendationConstants;
import com.fourtune.shared.search.event.SearchAuctionItemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 검색 로그 이벤트 Consumer
 * fourtune-api의 SearchKafkaProducer가 발행하는 search-log-events 토픽을 구독하여
 * 사용자의 검색 카테고리를 프로파일에 반영 (가중치 +1).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true")
public class SearchLogKafkaListener {

    private final ObjectMapper objectMapper;
    private final UserPreferenceService userPreferenceService;

    @KafkaListener(topics = KafkaTopicConfig.SEARCH_LOG_EVENTS_TOPIC, groupId = "recommendation-search-group", containerFactory = "auctionEventKafkaListenerContainerFactory")
    public void consume(String payload) {
        try {
            SearchAuctionItemEvent event = objectMapper.readValue(payload, SearchAuctionItemEvent.class);

            if (event.userId() == null) {
                log.debug("[REC][SEARCH] 비로그인 사용자 검색 — 프로파일링 생략");
                return;
            }

            List<String> categories = event.categories();
            if (categories == null || categories.isEmpty()) {
                log.debug("[REC][SEARCH] 카테고리 없는 검색 — 프로파일링 생략: keyword={}", event.keyword());
                return;
            }

            for (String category : categories) {
                userPreferenceService.incrementCategoryScore(event.userId(), category,
                        RecommendationConstants.SEARCH_WEIGHT);
            }

            log.info("[REC][SEARCH] 프로파일 반영 완료: userId={}, categories={}, keyword={}",
                    event.userId(), categories, event.keyword());

        } catch (Exception e) {
            log.error("[REC][SEARCH] 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }
}
