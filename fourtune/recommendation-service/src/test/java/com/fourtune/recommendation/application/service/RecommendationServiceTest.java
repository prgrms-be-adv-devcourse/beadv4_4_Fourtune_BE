package com.fourtune.recommendation.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import com.fourtune.recommendation.adapter.out.api.SearchClient;
import com.fourtune.recommendation.application.port.out.RecommendationAiClient;
import com.fourtune.recommendation.common.RecommendationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private UserPreferenceService userPreferenceService;
    @Mock
    private SearchClient searchClient;
    @Mock
    private RecommendationAiClient aiClient;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RecommendationService recommendationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        recommendationService = new RecommendationService(
                userPreferenceService, searchClient, aiClient, redisTemplate, objectMapper);
    }

    @Test
    @DisplayName("캐시 히트 시 Redis에서 바로 반환, AI/Feign 호출 없음")
    void getRecommendations_cacheHit_returnsFromCache() throws Exception {
        Long userId = 1L;
        List<RecommendedItemResponse> cached = List.of(createItem(100L, "테스트 상품"));
        String json = objectMapper.writeValueAsString(cached);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rec:user:1")).thenReturn(json);

        List<RecommendedItemResponse> result = recommendationService.getRecommendations(userId, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).auctionItemId()).isEqualTo(100L);
        verify(searchClient, never()).searchAuctionItems(any(), any(), any(), any());
        verify(aiClient, never()).recommend(any(), any(), any());
    }

    @Test
    @DisplayName("캐시 미스 + 프로파일 없음 → 인기순 폴백")
    void getRecommendations_cacheMiss_noProfile_fallsBackToPopular() {
        Long userId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rec:user:1")).thenReturn(null);
        when(userPreferenceService.getTopCategories(userId, RecommendationConstants.TOP_CATEGORY_LIMIT))
                .thenReturn(Collections.emptyList());

        Map<String, Object> searchResult = createSearchResult(
                createItemMap(200L, "인기 상품", "전자기기"));
        when(searchClient.searchAuctionItems(isNull(), eq(Set.of("ACTIVE")), eq("POPULAR"), eq(1)))
                .thenReturn(searchResult);

        List<RecommendedItemResponse> result = recommendationService.getRecommendations(userId, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("인기 상품");
        verify(aiClient, never()).recommend(any(), any(), any());
    }

    @Test
    @DisplayName("캐시 미스 + 프로파일 있음 → Feign 조회 + AI 재정렬")
    void getRecommendations_cacheMiss_withProfile_callsAi() {
        Long userId = 1L;
        List<String> categories = List.of("전자기기", "패션");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rec:user:1")).thenReturn(null);
        when(userPreferenceService.getTopCategories(userId, RecommendationConstants.TOP_CATEGORY_LIMIT))
                .thenReturn(categories);

        Map<String, Object> searchResult = createSearchResult(
                createItemMap(300L, "상품A", "전자기기"),
                createItemMap(301L, "상품B", "패션"));
        when(searchClient.searchAuctionItems(any(), any(), any(), any())).thenReturn(searchResult);

        List<RecommendedItemResponse> aiRanked = List.of(
                createItem(301L, "상품B"), createItem(300L, "상품A"));
        when(aiClient.recommend(eq(userId), eq(categories), any())).thenReturn(aiRanked);

        List<RecommendedItemResponse> result = recommendationService.getRecommendations(userId, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).auctionItemId()).isEqualTo(301L); // AI가 재정렬한 순서
        verify(aiClient).recommend(eq(userId), eq(categories), any());
    }

    @Test
    @DisplayName("Feign 호출 실패 시 인기순 폴백")
    void getRecommendations_feignFails_fallsBackToPopular() {
        Long userId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rec:user:1")).thenReturn(null);
        when(userPreferenceService.getTopCategories(userId, RecommendationConstants.TOP_CATEGORY_LIMIT))
                .thenReturn(List.of("전자기기"));
        when(searchClient.searchAuctionItems(any(Set.class), eq(Set.of("ACTIVE")), eq("POPULAR"), eq(1)))
                .thenThrow(new RuntimeException("Connection refused"));

        Map<String, Object> popularResult = createSearchResult(
                createItemMap(400L, "인기 폴백", "기타"));
        when(searchClient.searchAuctionItems(isNull(), eq(Set.of("ACTIVE")), eq("POPULAR"), eq(1)))
                .thenReturn(popularResult);

        List<RecommendedItemResponse> result = recommendationService.getRecommendations(userId, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("인기 폴백");
    }

    @Test
    @DisplayName("인기 상품 조회 - 정상 동작")
    void getPopularItems_success() {
        Map<String, Object> result = createSearchResult(
                createItemMap(500L, "인기1", "전자기기"),
                createItemMap(501L, "인기2", "패션"));
        when(searchClient.searchAuctionItems(isNull(), eq(Set.of("ACTIVE")), eq("POPULAR"), eq(1)))
                .thenReturn(result);

        List<RecommendedItemResponse> items = recommendationService.getPopularItems(10);

        assertThat(items).hasSize(2);
    }

    // ── 헬퍼 메서드 ──

    private RecommendedItemResponse createItem(Long id, String title) {
        return new RecommendedItemResponse(id, title, "전자기기", "ACTIVE",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(50000), true,
                "thumb.jpg", LocalDateTime.now(), LocalDateTime.now().plusDays(7), 100, 10, 5);
    }

    @SafeVarargs
    private Map<String, Object> createSearchResult(Map<String, Object>... items) {
        return Map.of("items", List.of(items));
    }

    private Map<String, Object> createItemMap(Long id, String title, String category) {
        Map<String, Object> item = new HashMap<>();
        item.put("auctionItemId", id);
        item.put("title", title);
        item.put("category", category);
        item.put("status", "ACTIVE");
        item.put("currentPrice", 10000);
        item.put("buyNowPrice", 50000);
        item.put("buyNowEnabled", true);
        item.put("thumbnailUrl", "thumb.jpg");
        item.put("startAt", LocalDateTime.now().toString());
        item.put("endAt", LocalDateTime.now().plusDays(7).toString());
        item.put("viewCount", 100);
        item.put("watchlistCount", 10);
        item.put("bidCount", 5);
        return item;
    }
}
