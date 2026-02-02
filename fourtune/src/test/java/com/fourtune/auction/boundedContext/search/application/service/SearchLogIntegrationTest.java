package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.SearchLog;
import com.fourtune.auction.boundedContext.search.port.out.SearchLogRepository;
import com.fourtune.auction.shared.search.event.SearchAuctionItemEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RecordApplicationEvents
class SearchLogIntegrationTest {

    @Autowired
    private SearchFacade searchFacade;

    @MockitoBean
    private SearchQueryUseCase searchQueryUseCase;

    @Autowired
    private SearchLogRepository searchLogRepository; // Use real repository to verify persistence

    @Autowired
    private com.fourtune.auction.boundedContext.search.adapter.out.persistence.SearchLogJpaRepository jpaRepository; // Direct
                                                                                                                     // access
                                                                                                                     // for
                                                                                                                     // assertions

    @Autowired
    private ApplicationEvents events;

    @Test
    @DisplayName("검색어가 존재할 경우 검색 로그가 저장되고 이벤트가 발행된다.")
    void searchLogAndEventTest() {
        // given
        Long userId = 1L;
        String keyword = "Test Keyword";
        SearchCondition condition = new SearchCondition(keyword, Collections.emptySet(), null, Collections.emptySet(),
                null, 1);

        // when
        searchFacade.search(userId, condition);

        // then
        // 1. 실제 DB 저장 확인
        List<SearchLog> logs = jpaRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getUserId()).isEqualTo(userId);
        assertThat(logs.get(0).getKeyword()).isEqualTo(keyword);

        // 2. 이벤트 발행 확인
        long eventCount = events.stream(SearchAuctionItemEvent.class)
                .filter(event -> event.keyword().equals(keyword) && event.userId().equals(userId))
                .count();
        assertThat(eventCount).isEqualTo(1);

        // 3. 실제 검색 로직 호출 확인
        verify(searchQueryUseCase, times(1)).search(any());
    }

    @Test
    @DisplayName("검색어가 없을 경우 로그가 저장되지 않는다.")
    void noSearchLogWhenKeywordIsEmpty() {
        // given
        Long userId = 1L;
        String keyword = ""; // Empty keyword
        SearchCondition condition = new SearchCondition(keyword, Collections.emptySet(), null, Collections.emptySet(),
                null, 1);

        // when
        searchFacade.search(userId, condition);

        // then
        List<SearchLog> logs = jpaRepository.findAll();
        assertThat(logs).isEmpty(); // Verify DB directly

        long eventCount = events.stream(SearchAuctionItemEvent.class).count();
        assertThat(eventCount).isEqualTo(0);
    }
}
