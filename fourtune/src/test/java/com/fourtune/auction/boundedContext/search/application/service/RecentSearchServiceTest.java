package com.fourtune.auction.boundedContext.search.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecentSearchServiceTest {

    @InjectMocks
    private RecentSearchService recentSearchService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Test
    @DisplayName("최근 검색어를 저장한다.")
    void addKeyword() {
        // given: 테스트용 사용자 ID와 키워드 준비
        Long userId = 1L;
        String keyword = "test";
        String key = "recent_search:" + userId;

        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
        
        // when: 최근 검색어 저장 메서드 호출
        recentSearchService.addKeyword(userId, keyword);

        // then: Redis ZSet에 키워드가 추가되었는지 검증 (Score는 현재 시간)
        verify(zSetOperations).add(eq(key), eq(keyword), anyDouble());
    }

    @Test
    @DisplayName("최근 검색어 목록을 조회한다.")
    void getKeywords() {
        // given: Redis에서 반환될 키워드 목록 설정
        Long userId = 1L;
        String key = "recent_search:" + userId;
        Set<String> keywords = Set.of("keyword1", "keyword2");

        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
        given(zSetOperations.reverseRange(key, 0, 9)).willReturn(keywords);

        // when: 최근 검색어 목록 조회 메서드 호출
        List<String> result = recentSearchService.getKeywords(userId);

        // then: 반환된 목록이 예상된 키워드를 포함하는지 검증
        assertThat(result).containsExactlyInAnyOrder("keyword1", "keyword2");
    }

    @Test
    @DisplayName("최근 검색어를 삭제한다.")
    void removeKeyword() {
        // given: 삭제할 키워드 설정
        Long userId = 1L;
        String keyword = "test";
        String key = "recent_search:" + userId;

        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);

        // when: 최근 검색어 삭제 메서드 호출
        recentSearchService.removeKeyword(userId, keyword);

        // then: Redis ZSet에서 해당 키워드가 삭제되었는지 검증
        verify(zSetOperations).remove(key, keyword);
    }
}
