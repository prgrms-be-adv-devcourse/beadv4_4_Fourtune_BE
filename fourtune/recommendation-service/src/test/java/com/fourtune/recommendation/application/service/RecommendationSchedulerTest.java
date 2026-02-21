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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationSchedulerTest {

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

    private RecommendationScheduler scheduler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        scheduler = new RecommendationScheduler(
                userPreferenceService, searchClient, aiClient, redisTemplate, objectMapper);
    }

    @Test
    @DisplayName("활성 사용자가 없으면 스킵")
    void refreshRecommendations_noActiveUsers_skips() {
        when(redisTemplate.keys("metrics:user:*")).thenReturn(Collections.emptySet());

        scheduler.refreshRecommendations();

        verify(searchClient, never()).searchAuctionItems(any(), any(), any(), any());
        verify(aiClient, never()).recommend(any(), any(), any());
    }

    @Test
    @DisplayName("활성 사용자 있으면 Feign → AI → Redis 캐싱 호출")
    void refreshRecommendations_activeUser_generatesAndCaches() {
        Set<String> keys = Set.of("metrics:user:42");
        when(redisTemplate.keys("metrics:user:*")).thenReturn(keys);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(userPreferenceService.getTopCategories(42L, RecommendationConstants.TOP_CATEGORY_LIMIT))
                .thenReturn(List.of("전자기기", "패션"));

        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("items", List.of(createItemMap(100L, "상품A")));
        when(searchClient.searchAuctionItems(any(), any(), any(), any())).thenReturn(searchResult);

        List<RecommendedItemResponse> ranked = List.of(createItem(100L, "상품A"));
        when(aiClient.recommend(eq(42L), any(), any())).thenReturn(ranked);

        scheduler.refreshRecommendations();

        verify(searchClient).searchAuctionItems(any(), eq(Set.of("ACTIVE")), eq("POPULAR"), eq(1));
        verify(aiClient).recommend(eq(42L), any(), any());
        verify(valueOperations).set(eq("rec:user:42"), anyString(), any());
    }

    @Test
    @DisplayName("프로파일 없는 사용자는 건너뜀")
    void refreshRecommendations_noProfile_skipsUser() {
        when(redisTemplate.keys("metrics:user:*")).thenReturn(Set.of("metrics:user:99"));
        when(userPreferenceService.getTopCategories(99L, RecommendationConstants.TOP_CATEGORY_LIMIT))
                .thenReturn(Collections.emptyList());

        scheduler.refreshRecommendations();

        verify(searchClient, never()).searchAuctionItems(any(), any(), any(), any());
    }

    @Test
    @DisplayName("개별 사용자 실패해도 다른 사용자는 계속 처리")
    void refreshRecommendations_oneUserFails_continuesOthers() {
        Set<String> keys = new LinkedHashSet<>(List.of("metrics:user:1", "metrics:user:2"));
        when(redisTemplate.keys("metrics:user:*")).thenReturn(keys);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 사용자 1, 2 모두 카테고리 존재
        when(userPreferenceService.getTopCategories(anyLong(), eq(RecommendationConstants.TOP_CATEGORY_LIMIT)))
                .thenReturn(List.of("전자기기"));

        // 첫 번째 호출(사용자 1)은 실패, 두 번째 호출(사용자 2)은 성공
        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("items", List.of(createItemMap(200L, "패션상품")));
        when(searchClient.searchAuctionItems(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("fail"))
                .thenReturn(searchResult);

        when(aiClient.recommend(eq(2L), any(), any()))
                .thenReturn(List.of(createItem(200L, "패션상품")));

        scheduler.refreshRecommendations();

        // 사용자 2의 AI 호출이 발생했는지 확인
        verify(aiClient).recommend(eq(2L), any(), any());
    }

    // ── 헬퍼 ──

    private RecommendedItemResponse createItem(Long id, String title) {
        return new RecommendedItemResponse(id, title, "카테고리", "ACTIVE",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(50000), true,
                "thumb.jpg", LocalDateTime.now(), LocalDateTime.now().plusDays(7), 100, 10, 5);
    }

    private Map<String, Object> createItemMap(Long id, String title) {
        Map<String, Object> item = new HashMap<>();
        item.put("auction_item_id", id);
        item.put("title", title);
        item.put("category", "전자기기");
        item.put("status", "ACTIVE");
        item.put("current_price", 10000);
        item.put("buy_now_price", 50000);
        item.put("buy_now_enabled", true);
        item.put("thumbnail_url", "thumb.jpg");
        item.put("start_at", LocalDateTime.now().toString());
        item.put("end_at", LocalDateTime.now().plusDays(7).toString());
        item.put("view_count", 100);
        item.put("watchlist_count", 10);
        item.put("bid_count", 5);
        return item;
    }
}
