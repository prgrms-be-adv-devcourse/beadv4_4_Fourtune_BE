package com.fourtune.recommendation.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import com.fourtune.recommendation.adapter.out.api.SearchClient;
import com.fourtune.recommendation.application.port.out.RecommendationAiClient;
import com.fourtune.recommendation.common.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 추천 서비스 핵심 비즈니스 로직.
 *
 * 흐름:
 * 1. Redis 캐시 확인 → 캐시 히트 시 즉시 반환 (AI 호출 없음)
 * 2. 캐시 미스 → Redis 프로파일에서 상위 카테고리 조회
 * 3. Search API (Feign) 호출 → 해당 카테고리의 ACTIVE 상품 조회
 * 4. AI 엔진으로 재정렬 → 결과를 Redis에 캐싱 후 반환
 *
 * 프로파일 없는 사용자 → 인기순(POPULAR) 기본 추천 제공
 * 배치 갱신: RecommendationScheduler가 주기적으로 캐시를 사전 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserPreferenceService userPreferenceService;
    private final SearchClient searchClient;
    private final RecommendationAiClient aiClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${recommendation.cache.ttl:86400}")
    private long cacheTtlSeconds;

    /**
     * 인증된 사용자 기반 개인화 추천 상품 반환.
     * 캐시 우선 조회 → 캐시 미스 시 실시간 생성.
     */
    public List<RecommendedItemResponse> getRecommendations(Long userId, int size) {
        // 1. 캐시 확인 (스케줄러가 미리 생성해둔 결과)
        String cacheKey = RecommendationConstants.RECOMMENDATION_CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("[REC] 캐시 히트: userId={}", userId);
            try {
                List<RecommendedItemResponse> items = objectMapper.readValue(
                        cached, new TypeReference<List<RecommendedItemResponse>>() {});
                return items.stream().limit(size).collect(Collectors.toList());
            } catch (Exception e) {
                log.error("[REC] 캐시 역직렬화 실패, 실시간 생성으로 폴백: {}", e.getMessage());
            }
        }

        // 2. 캐시 미스 → 실시간 생성
        log.info("[REC] 캐시 미스: userId={}, 실시간 추천 생성", userId);
        return generateRecommendations(userId, size);
    }

    /**
     * 실시간 추천 생성 (캐시 미스 시, 또는 첫 방문 시).
     */
    private List<RecommendedItemResponse> generateRecommendations(Long userId, int size) {
        List<String> topCategories = userPreferenceService.getTopCategories(userId,
                RecommendationConstants.TOP_CATEGORY_LIMIT);

        if (topCategories.isEmpty()) {
            log.info("[REC] 프로파일 없음 (userId={}), 인기순 기본 추천 제공", userId);
            return getPopularItems(size);
        }

        log.info("[REC] 개인화 추천: userId={}, topCategories={}", userId, topCategories);

        try {
            Map<String, Object> result = searchClient.searchAuctionItems(
                    new HashSet<>(topCategories),
                    Set.of("ACTIVE"),
                    "POPULAR",
                    1);
            List<RecommendedItemResponse> candidates = parseItems(result, size);

            // AI 추천 엔진으로 재정렬
            List<RecommendedItemResponse> ranked = aiClient.recommend(userId, topCategories, candidates);

            // 캐시 저장
            try {
                String cacheKey = RecommendationConstants.RECOMMENDATION_CACHE_PREFIX + userId;
                String json = objectMapper.writeValueAsString(ranked);
                redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(cacheTtlSeconds));
            } catch (Exception e) {
                log.warn("[REC] 캐시 저장 실패 (추천은 정상 반환): {}", e.getMessage());
            }

            return ranked;

        } catch (Exception e) {
            log.error("[REC] Search API 호출 실패, 인기순 폴백: {}", e.getMessage());
            return getPopularItems(size);
        }
    }

    /**
     * 비로그인 사용자 또는 프로파일 미존재 시 인기순 상품 반환.
     */
    public List<RecommendedItemResponse> getPopularItems(int size) {
        try {
            Map<String, Object> result = searchClient.searchAuctionItems(
                    null,
                    Set.of("ACTIVE"),
                    "POPULAR",
                    1);
            return parseItems(result, size);

        } catch (Exception e) {
            log.error("[REC] Search API 호출 실패 (popular): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<RecommendedItemResponse> parseItems(Map<String, Object> result, int size) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.stream()
                .limit(size)
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

    private Long toLong(Object v) { return v instanceof Number n ? n.longValue() : null; }
    private long toLongValue(Object v) { return v instanceof Number n ? n.longValue() : 0L; }
    private int toInt(Object v) { return v instanceof Number n ? n.intValue() : 0; }

    private BigDecimal toBigDecimal(Object v) {
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (v instanceof String s) return new BigDecimal(s);
        return null;
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v instanceof String s) {
            try { return LocalDateTime.parse(s); } catch (Exception e) { return null; }
        }
        return null;
    }
}
