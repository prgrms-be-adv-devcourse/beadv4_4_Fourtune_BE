package com.fourtune.recommendation.application.service;

import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import com.fourtune.recommendation.adapter.out.api.SearchClient;
import com.fourtune.recommendation.application.port.out.RecommendationAiClient;
import com.fourtune.recommendation.common.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 추천 서비스 핵심 비즈니스 로직.
 * 
 * 흐름:
 * 1. Redis에서 사용자 상위 카테고리 조회
 * 2. Search API (Feign) 호출 → 해당 카테고리의 ACTIVE 상품 조회
 * 3. 결과를 RecommendedItemResponse로 변환하여 반환
 *
 * 프로파일 없는 사용자 → 인기순(POPULAR) 기본 추천 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserPreferenceService userPreferenceService;
    private final SearchClient searchClient;
    private final RecommendationAiClient aiClient;

    /**
     * 인증된 사용자 기반 개인화 추천 상품 반환.
     */
    public List<RecommendedItemResponse> getRecommendations(Long userId, int size) {
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

            // AI 추천 엔진을 통한 후처리 (랭킹 조정 등)
            return aiClient.recommend(userId, topCategories, candidates);

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

    private Long toLong(Object value) {
        if (value instanceof Number num)
            return num.longValue();
        return null;
    }

    private long toLongValue(Object value) {
        if (value instanceof Number num)
            return num.longValue();
        return 0L;
    }

    private int toInt(Object value) {
        if (value instanceof Number num)
            return num.intValue();
        return 0;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number num)
            return BigDecimal.valueOf(num.doubleValue());
        if (value instanceof String str)
            return new BigDecimal(str);
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof String str) {
            try {
                return LocalDateTime.parse(str);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
