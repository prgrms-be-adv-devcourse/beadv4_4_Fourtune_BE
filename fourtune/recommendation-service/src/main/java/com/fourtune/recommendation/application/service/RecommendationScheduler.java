package com.fourtune.recommendation.application.service;

import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import com.fourtune.recommendation.adapter.out.api.SearchClient;
import com.fourtune.recommendation.application.port.out.RecommendationAiClient;
import com.fourtune.recommendation.common.RecommendationConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 추천 스케줄러.
 * 주기적으로 활성 사용자들의 AI 추천을 배치 생성하여 Redis에 캐싱합니다.
 *
 * 흐름: Redis(사용자 프로파일) → Feign(상품 조회) → AI(재정렬) → Redis(캐시 저장)
 * 스케줄 주기: application.yml의 recommendation.schedule.interval (기본 24시간, 로컬 30초)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final UserPreferenceService userPreferenceService;
    private final SearchClient searchClient;
    private final RecommendationAiClient aiClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${recommendation.cache.ttl:86400}")
    private long cacheTtlSeconds;

    /**
     * 활성 사용자들의 추천 결과를 배치로 갱신합니다.
     * 프로파일이 존재하는 사용자(metrics:user:*)를 대상으로 합니다.
     */
    @Scheduled(fixedRateString = "${recommendation.schedule.interval}")
    public void refreshRecommendations() {
        log.info("[REC][SCHEDULER] 추천 배치 갱신 시작");

        Set<String> userKeys = redisTemplate.keys(RecommendationConstants.USER_METRICS_KEY_PREFIX + "*");
        if (userKeys == null || userKeys.isEmpty()) {
            log.info("[REC][SCHEDULER] 활성 사용자 없음, 스킵");
            return;
        }

        int success = 0;
        int fail = 0;

        for (String key : userKeys) {
            try {
                Long userId = extractUserId(key);
                if (userId == null)
                    continue;

                List<String> topCategories = userPreferenceService.getTopCategories(
                        userId, RecommendationConstants.TOP_CATEGORY_LIMIT);
                if (topCategories.isEmpty())
                    continue;

                // 1. Feign으로 해당 카테고리 상품 조회
                Map<String, Object> searchResult = searchClient.searchAuctionItems(
                        new HashSet<>(topCategories),
                        Set.of("ACTIVE"),
                        "POPULAR",
                        1);

                List<RecommendedItemResponse> candidates = parseItems(searchResult);
                if (candidates.isEmpty())
                    continue;

                // 2. AI로 재정렬
                List<RecommendedItemResponse> ranked = aiClient.recommend(userId, topCategories, candidates);

                // 3. Redis에 캐싱
                String cacheKey = RecommendationConstants.RECOMMENDATION_CACHE_PREFIX + userId;
                String json = objectMapper.writeValueAsString(ranked);
                redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(cacheTtlSeconds));

                success++;
                log.debug("[REC][SCHEDULER] userId={} 추천 갱신 완료 ({}개)", userId, ranked.size());

            } catch (Exception e) {
                fail++;
                log.error("[REC][SCHEDULER] 사용자 추천 갱신 실패: key={}, error={}", key, e.getMessage());
            }
        }

        log.info("[REC][SCHEDULER] 추천 배치 완료: 성공={}, 실패={}, 전체={}", success, fail, userKeys.size());
    }

    private Long extractUserId(String key) {
        try {
            String idStr = key.replace(RecommendationConstants.USER_METRICS_KEY_PREFIX, "");
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<RecommendedItemResponse> parseItems(Map<String, Object> result) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.stream()
                .limit(RecommendationConstants.MAX_SIZE)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RecommendedItemResponse toResponse(Map<String, Object> item) {
        return new RecommendedItemResponse(
                toLong(item.get("auction_item_id")),
                (String) item.get("title"),
                (String) item.get("category"),
                (String) item.get("status"),
                toBigDecimal(item.get("current_price")),
                toBigDecimal(item.get("buy_now_price")),
                (Boolean) item.get("buy_now_enabled"),
                (String) item.get("thumbnail_url"),
                toLocalDateTime(item.get("start_at")),
                toLocalDateTime(item.get("end_at")),
                toLongValue(item.get("view_count")),
                toInt(item.get("watchlist_count")),
                toInt(item.get("bid_count")));
    }

    private Long toLong(Object v) {
        return v instanceof Number n ? n.longValue() : null;
    }

    private long toLongValue(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }

    private int toInt(Object v) {
        return v instanceof Number n ? n.intValue() : 0;
    }

    private java.math.BigDecimal toBigDecimal(Object v) {
        if (v instanceof Number n)
            return java.math.BigDecimal.valueOf(n.doubleValue());
        if (v instanceof String s)
            return new java.math.BigDecimal(s);
        return null;
    }

    private java.time.LocalDateTime toLocalDateTime(Object v) {
        if (v instanceof String s) {
            try {
                return java.time.LocalDateTime.parse(s);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
