package com.fourtune.recommendation.application.service;

import com.fourtune.recommendation.common.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자 선호도 프로파일 서비스.
 * Redis Hash (metrics:user:{userId}) 에 카테고리별 점수를 관리.
 * 
 * 점수 가중치: 검색 → +1, 찜하기 ADD → +3, 찜하기 REMOVE → -3, 입찰 → +5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 카테고리 점수를 증감시킵니다 (HINCRBY).
     * weight가 음수이면 점수가 감소합니다.
     */
    public void incrementCategoryScore(Long userId, String category, int weight) {
        if (userId == null || category == null || category.isBlank()) {
            return;
        }
        String key = RecommendationConstants.USER_METRICS_KEY_PREFIX + userId;
        String field = RecommendationConstants.CATEGORY_FIELD_PREFIX + category;
        redisTemplate.opsForHash().increment(key, field, weight);
        log.debug("[REC][PROFILE] userId={}, category={}, weight={}", userId, category, weight);
    }

    /**
     * 사용자의 상위 N개 선호 카테고리를 점수 내림차순으로 반환합니다.
     *
     * @return 카테고리 이름 리스트 (점수가 0 이하인 항목 제외)
     */
    public List<String> getTopCategories(Long userId, int limit) {
        String key = RecommendationConstants.USER_METRICS_KEY_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return Collections.emptyList();
        }

        return entries.entrySet().stream()
                .filter(e -> e.getKey().toString().startsWith(RecommendationConstants.CATEGORY_FIELD_PREFIX))
                .map(e -> Map.entry(
                        e.getKey().toString().replace(RecommendationConstants.CATEGORY_FIELD_PREFIX, ""),
                        Long.parseLong(e.getValue().toString())))
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
