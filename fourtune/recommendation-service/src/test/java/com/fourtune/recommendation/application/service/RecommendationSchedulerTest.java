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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
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
        Cursor<String> cursor = emptyCursor();
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        scheduler.refreshRecommendations();

        verify(searchClient, never()).searchAuctionItems(any(), any(), any(), any());
        verify(aiClient, never()).recommend(any(), any(), any());
    }

    @Test
    @DisplayName("활성 사용자 있으면 Feign → AI → Redis 캐싱 호출")
    void refreshRecommendations_activeUser_generatesAndCaches() {
        Cursor<String> cursor = cursorOf("metrics:user:42");
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
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
        Cursor<String> cursor = cursorOf("metrics:user:99");
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(userPreferenceService.getTopCategories(99L, RecommendationConstants.TOP_CATEGORY_LIMIT))
                .thenReturn(Collections.emptyList());

        scheduler.refreshRecommendations();

        verify(searchClient, never()).searchAuctionItems(any(), any(), any(), any());
    }

    @Test
    @DisplayName("개별 사용자 실패해도 다른 사용자는 계속 처리")
    void refreshRecommendations_oneUserFails_continuesOthers() {
        Cursor<String> cursor = cursorOf("metrics:user:1", "metrics:user:2");
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 사용자 1, 2 모두 카테고리 존재
        when(userPreferenceService.getTopCategories(anyLong(), eq(RecommendationConstants.TOP_CATEGORY_LIMIT)))
                .thenReturn(List.of("전자기기"));

        // 첫 번째 호출은 실패, 두 번째 호출은 성공
        // (HashSet 특성상 순서가 비결정적이므로 어떤 사용자가 먼저 처리될지 알 수 없음)
        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("items", List.of(createItemMap(200L, "패션상품")));
        when(searchClient.searchAuctionItems(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("fail"))
                .thenReturn(searchResult);

        when(aiClient.recommend(anyLong(), any(), any()))
                .thenReturn(List.of(createItem(200L, "패션상품")));

        scheduler.refreshRecommendations();

        // 핵심 검증: 한 명이 실패해도 루프가 중단되지 않고 나머지 사용자를 처리함
        // searchClient는 2번 호출 (1번 실패 + 1번 성공), aiClient는 성공한 1명에 대해 1번만 호출
        verify(searchClient, times(2)).searchAuctionItems(any(), any(), any(), any());
        verify(aiClient, times(1)).recommend(anyLong(), any(), any());
    }

    // ── 헬퍼 ──

    @SuppressWarnings("unchecked")
    private Cursor<String> emptyCursor() {
        Cursor<String> cursor = mock(Cursor.class);
        doReturn(false).when(cursor).hasNext();
        return cursor;
    }

    @SuppressWarnings("unchecked")
    private Cursor<String> cursorOf(String... keys) {
        Cursor<String> cursor = mock(Cursor.class);
        Iterator<String> iterator = Arrays.asList(keys).iterator();
        doAnswer(inv -> iterator.hasNext()).when(cursor).hasNext();
        doAnswer(inv -> iterator.next()).when(cursor).next();
        return cursor;
    }

    private RecommendedItemResponse createItem(Long id, String title) {
        return new RecommendedItemResponse(id, title, "카테고리", "ACTIVE",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(50000), true,
                "thumb.jpg", LocalDateTime.now(), LocalDateTime.now().plusDays(7), 100, 10, 5);
    }

    private Map<String, Object> createItemMap(Long id, String title) {
        Map<String, Object> item = new HashMap<>();
        item.put("auctionItemId", id);
        item.put("title", title);
        item.put("category", "전자기기");
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
