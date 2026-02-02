package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.SearchLog;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.auction.boundedContext.search.port.out.SearchLogRepository;
import com.fourtune.auction.shared.search.event.SearchAuctionItemEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SearchFacade {
    private final SearchQueryUseCase queryUseCase;
    private final SearchLogRepository searchLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SearchResultPage<SearchAuctionItemView> search(Long userId, SearchCondition condition) {
        // 1. 검색 로그 저장 및 이벤트 발행 (비동기 고려 가능하지만, Phase 0에서는 동기 처리하고 추후 리팩토링 하기로)
        if (StringUtils.hasText(condition.keyword())) {
            saveSearchLog(userId, condition.keyword());
            publishSearchEvent(userId, condition.keyword());
        }

        // 2. 검색 수행
        return queryUseCase.search(condition);
    }

    private void saveSearchLog(Long userId, String keyword) {
        SearchLog log = SearchLog.builder()
                .userId(userId)
                .keyword(keyword)
                .build();
        searchLogRepository.save(log);
    }

    private void publishSearchEvent(Long userId, String keyword) {
        eventPublisher.publishEvent(new SearchAuctionItemEvent(
                userId,
                keyword,
                LocalDateTime.now()));
    }
}
