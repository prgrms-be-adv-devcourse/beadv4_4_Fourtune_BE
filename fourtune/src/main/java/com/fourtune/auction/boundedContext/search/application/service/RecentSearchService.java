package com.fourtune.auction.boundedContext.search.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecentSearchService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "recent_search:";
    private static final int MAX_KEYWORDS = 10;

    /**
     * 최근 검색어 저장 (비동기)
     * - 중복 키워드는 최신 Score로 갱신
     * - 최대 개수 초과 시 오래된 순 삭제
     */
    @Async
    public void addKeyword(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.isBlank()) {
            return;
        }

        String key = KEY_PREFIX + userId;
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        try {
            // 1. ZADD: 키워드 추가 (Score = 현재 시간) - 이미 존재하면 Score 갱신됨
            zSetOps.add(key, keyword, System.currentTimeMillis());

            // 2. ZREMRANGEBYRANK: 0 ~ -11 (뒤에서 11번째까지 남기고 삭제 -> 최근 10개 유지)
            // rank는 0부터 시작하므로, 0 ~ -(MAX_KEYWORDS + 1) 범위를 삭제하면 상위 N개만 남음
            // 하지만 리스트 크기를 구해서 삭제하는 것보다, 전체 개수가 N개를 넘을 때만 삭제하는 것이 효율적일 수 있음.
            // 여기서는 간단하게 removeRange로 처리: 0 (가장 오래된 것) ~ (총개수 - MAX_KEYWORDS - 1) 삭제
            
            Long count = zSetOps.zCard(key);
            if (count != null && count > MAX_KEYWORDS) {
                zSetOps.removeRange(key, 0, count - MAX_KEYWORDS - 1);
            }
            
            // (선택) TTL 설정: 30일
            // redisTemplate.expire(key, java.time.Duration.ofDays(30));

        } catch (Exception e) {
            log.error("Failed to add recent search keyword for user {}: {}", userId, e.getMessage());
        }
    }

    // 최근 검색어 목록 조회 - 최신순 정렬
    public List<String> getKeywords(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        String key = KEY_PREFIX + userId;
        
        // Score 역순(최신순) 조회
        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(key, 0, MAX_KEYWORDS - 1);
        return keywords != null ? new ArrayList<>(keywords) : Collections.emptyList();
    }

    // 최근 검색어 개별 삭제
    public void removeKeyword(Long userId, String keyword) {
        if (userId == null || keyword == null) {
            return;
        }
        String key = KEY_PREFIX + userId;
        redisTemplate.opsForZSet().remove(key, keyword);
    }
}
