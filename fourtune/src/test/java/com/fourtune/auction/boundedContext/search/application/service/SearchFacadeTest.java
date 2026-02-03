package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.port.out.SearchLogRepository;
import com.fourtune.auction.shared.search.event.SearchAuctionItemEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchFacadeTest {

    @InjectMocks
    private SearchFacade searchFacade;

    @Mock
    private SearchQueryUseCase searchQueryUseCase;

    @Mock
    private SearchLogRepository searchLogRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("검색어가 존재할 경우 검색 로그가 저장되고 이벤트가 발행된다.")
    void search_WithKeyword_ShouldSaveLogAndPublishEvent() {
        // given
        Long userId = 1L;
        String keyword = "test keyword";
        SearchCondition condition = new SearchCondition(keyword, Collections.emptySet(), null, Collections.emptySet(),
                null, 1);

        SearchResultPage<SearchAuctionItemView> emptyResult = new SearchResultPage<>(Collections.emptyList(), 0, 1, 10,
                false);
        org.mockito.BDDMockito.given(searchQueryUseCase.search(condition)).willReturn(emptyResult);

        // when
        searchFacade.search(userId, condition);

        // then
        // 1. 검색 유스케이스 호출 검증 (먼저 호출됨)
        verify(searchQueryUseCase, times(1)).search(condition);

        // 2. 로그 저장 검증
        verify(searchLogRepository, times(1)).save(any());

        // 3. 이벤트 발행 검증
        verify(eventPublisher, times(1)).publishEvent(any(SearchAuctionItemEvent.class));
    }

    @Test
    @DisplayName("검색어가 없을 경우 로그 저장 및 이벤트 발행이 되지 않는다.")
    void search_WithoutKeyword_ShouldNotSaveLogOrPublishEvent() {
        // given
        Long userId = 1L;
        String keyword = "";
        SearchCondition condition = new SearchCondition(keyword, Collections.emptySet(), null, Collections.emptySet(),
                null, 1);

        SearchResultPage<SearchAuctionItemView> emptyResult = new SearchResultPage<>(Collections.emptyList(), 0, 1, 10,
                false);
        org.mockito.BDDMockito.given(searchQueryUseCase.search(condition)).willReturn(emptyResult);

        // when
        searchFacade.search(userId, condition);

        // then
        // 1. 검색 유스케이스는 호출되어야 함
        verify(searchQueryUseCase, times(1)).search(condition);

        // 2. 로그 저장 안됨
        verify(searchLogRepository, times(0)).save(any());

        // 3. 이벤트 발행 안됨
        verify(eventPublisher, times(0)).publishEvent(any());
    }
}
