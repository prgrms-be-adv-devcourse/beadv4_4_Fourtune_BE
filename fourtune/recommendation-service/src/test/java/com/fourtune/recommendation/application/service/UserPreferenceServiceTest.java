package com.fourtune.recommendation.application.service;

import com.fourtune.recommendation.common.RecommendationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private UserPreferenceService userPreferenceService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("카테고리 점수 증가 - 정상 동작")
    void incrementCategoryScore_success() {
        Long userId = 1L;
        String category = "전자기기";
        int weight = RecommendationConstants.SEARCH_WEIGHT;

        userPreferenceService.incrementCategoryScore(userId, category, weight);

        verify(hashOperations).increment(
                eq("metrics:user:1"),
                eq("category:전자기기"),
                eq((long) weight));
    }

    @Test
    @DisplayName("카테고리 점수 증가 - userId null이면 무시")
    void incrementCategoryScore_nullUserId_ignored() {
        userPreferenceService.incrementCategoryScore(null, "전자기기", 1);
        verify(hashOperations, never()).increment(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("카테고리 점수 증가 - 빈 카테고리이면 무시")
    void incrementCategoryScore_blankCategory_ignored() {
        userPreferenceService.incrementCategoryScore(1L, "", 1);
        verify(hashOperations, never()).increment(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("상위 카테고리 조회 - 점수 내림차순 정렬")
    void getTopCategories_sortedByScoreDesc() {
        Long userId = 1L;
        Map<Object, Object> entries = new HashMap<>();
        entries.put("category:전자기기", "10");
        entries.put("category:패션", "30");
        entries.put("category:도서", "20");

        when(hashOperations.entries("metrics:user:1")).thenReturn(entries);

        List<String> result = userPreferenceService.getTopCategories(userId, 3);

        assertThat(result).containsExactly("패션", "도서", "전자기기");
    }

    @Test
    @DisplayName("상위 카테고리 조회 - 0점 이하 항목 제외")
    void getTopCategories_excludesNonPositive() {
        Long userId = 1L;
        Map<Object, Object> entries = new HashMap<>();
        entries.put("category:전자기기", "10");
        entries.put("category:패션", "0");
        entries.put("category:도서", "-3");

        when(hashOperations.entries("metrics:user:1")).thenReturn(entries);

        List<String> result = userPreferenceService.getTopCategories(userId, 3);

        assertThat(result).containsExactly("전자기기");
    }

    @Test
    @DisplayName("상위 카테고리 조회 - 프로파일 없으면 빈 리스트")
    void getTopCategories_emptyProfile() {
        Long userId = 1L;
        when(hashOperations.entries("metrics:user:1")).thenReturn(new HashMap<>());

        List<String> result = userPreferenceService.getTopCategories(userId, 3);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상위 카테고리 조회 - limit 적용")
    void getTopCategories_respectsLimit() {
        Long userId = 1L;
        Map<Object, Object> entries = new HashMap<>();
        entries.put("category:전자기기", "10");
        entries.put("category:패션", "30");
        entries.put("category:도서", "20");
        entries.put("category:스포츠", "5");

        when(hashOperations.entries("metrics:user:1")).thenReturn(entries);

        List<String> result = userPreferenceService.getTopCategories(userId, 2);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("패션", "도서");
    }
}
