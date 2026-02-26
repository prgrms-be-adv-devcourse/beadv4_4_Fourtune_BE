package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.policy.RecentSearchPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecentSearchService {

    private final StringRedisTemplate redisTemplate;
    private final RecentSearchPolicy recentSearchPolicy;

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

        String key = recentSearchPolicy.getKeyPrefix() + userId;
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        try {
            // 1. ZADD: 키워드 추가 (Score = 현재 시간) - 이미 존재하면 Score 갱신됨
            zSetOps.add(key, keyword, System.currentTimeMillis());

            // 2. 최대 개수 초과 시 오래된 순 삭제
            Long count = zSetOps.zCard(key);
            int maxKeywords = recentSearchPolicy.getMaxKeywords();

            if (count != null && count > maxKeywords) {
                zSetOps.removeRange(key, 0, count - maxKeywords - 1);
            }

            // 3. TTL 설정
            redisTemplate.expire(key, recentSearchPolicy.getTtl());

        } catch (Exception e) {
            log.error("Failed to add recent search keyword for user {}: {}", userId, e.getMessage());
        }
    }

    // 최근 검색어 목록 조회 - 최신순 정렬
    public List<String> getKeywords(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        String key = recentSearchPolicy.getKeyPrefix() + userId;
        int maxKeywords = recentSearchPolicy.getMaxKeywords();

        // Score 역순(최신순) 조회
        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(key, 0, maxKeywords - 1);
        return keywords != null ? new ArrayList<>(keywords) : Collections.emptyList();
    }

    // 최근 검색어 개별 삭제
    public void removeKeyword(Long userId, String keyword) {
        if (userId == null || keyword == null) {
            return;
        }
        String key = recentSearchPolicy.getKeyPrefix() + userId;
        redisTemplate.opsForZSet().remove(key, keyword);
    }

    // 최근 검색어 전체 삭제
    public void removeAllKeywords(Long userId) {
        if (userId == null) {
            return;
        }
        String key = recentSearchPolicy.getKeyPrefix() + userId;
        redisTemplate.delete(key);
    }
}
