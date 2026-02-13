package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.policy.RecentSearchPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
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

    @Mock
    private RecentSearchPolicy recentSearchPolicy;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_KEY = "recent_search:" + TEST_USER_ID; // 서비스 내부 규칙과 동일하게 설정
    private static final String TEST_KEYWORD = "test";
    private static final int TEST_MAX_KEYWORDS = 10;
    private static final Duration TEST_TTL = Duration.ofDays(30);

    @Test
    @DisplayName("최근 검색어를 저장한다.")
    void addKeyword() {
        // given
        // Policy Mocking
        given(recentSearchPolicy.getKeyPrefix()).willReturn("recent_search:");
        given(recentSearchPolicy.getMaxKeywords()).willReturn(TEST_MAX_KEYWORDS);
        given(recentSearchPolicy.getTtl()).willReturn(TEST_TTL);

        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
        
        // when
        recentSearchService.addKeyword(TEST_USER_ID, TEST_KEYWORD);

        // then: Redis ZSet에 키워드가 추가되었는지 검증
        verify(zSetOperations).add(eq(TEST_KEY), eq(TEST_KEYWORD), anyDouble());
        // then: TTL 설정 검증 (Policy에서 반환한 값과 일치하는지 확인)
        verify(redisTemplate).expire(eq(TEST_KEY), eq(TEST_TTL));
    }

    @Test
    @DisplayName("검색어가 최대 개수를 초과하면 가장 오래된 검색어를 삭제한다.")
    void addKeyword_WithMaxLimit() {
        // given
        String newKeyword = "new_keyword";

        // Policy Mocking
        given(recentSearchPolicy.getKeyPrefix()).willReturn("recent_search:");
        given(recentSearchPolicy.getMaxKeywords()).willReturn(TEST_MAX_KEYWORDS);
        given(recentSearchPolicy.getTtl()).willReturn(TEST_TTL);

        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
        // 저장된 개수가 MAX + 1개라고 가정
        given(zSetOperations.zCard(TEST_KEY)).willReturn((long) (TEST_MAX_KEYWORDS + 1)); 

        // when
        recentSearchService.addKeyword(TEST_USER_ID, newKeyword);

        // then: 가장 오래된 1개 삭제 로직 호출 검증 (removeRange(0, 0))
        // count(MAX+1) - MAX - 1 = 0
        verify(zSetOperations).removeRange(TEST_KEY, 0, 0);
    }

    @Test
    @DisplayName("최근 검색어 목록을 조회한다.")
    void getKeywords() {
        // given
        Set<String> keywords = Set.of("keyword1", "keyword2");

        // Policy Mocking
        given(recentSearchPolicy.getKeyPrefix()).willReturn("recent_search:");
        given(recentSearchPolicy.getMaxKeywords()).willReturn(TEST_MAX_KEYWORDS);

        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
        // 0 ~ (MAX-1) 범위 조회
        given(zSetOperations.reverseRange(TEST_KEY, 0, TEST_MAX_KEYWORDS - 1)).willReturn(keywords);

        // when
        List<String> result = recentSearchService.getKeywords(TEST_USER_ID);

        // then
        assertThat(result).containsExactlyInAnyOrder("keyword1", "keyword2");
    }

    @Test
    @DisplayName("최근 검색어를 삭제한다.")
    void removeKeyword() {
        // given
        given(recentSearchPolicy.getKeyPrefix()).willReturn("recent_search:");
        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);

        // when
        recentSearchService.removeKeyword(TEST_USER_ID, TEST_KEYWORD);

        // then
        verify(zSetOperations).remove(TEST_KEY, TEST_KEYWORD);
    }
}
