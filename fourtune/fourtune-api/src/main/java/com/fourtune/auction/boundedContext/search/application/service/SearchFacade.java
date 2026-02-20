package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.shared.search.event.SearchAuctionItemEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SearchFacade {
    private final SearchQueryUseCase queryUseCase;
    private final RecentSearchService recentSearchService;
    private final ApplicationEventPublisher eventPublisher;

    public SearchResultPage<SearchAuctionItemView> search(Long userId, SearchCondition condition) {
        // 1. 검색 수행
        SearchResultPage<SearchAuctionItemView> result = queryUseCase.search(condition);

        // 2. 검색 로그 저장 및 이벤트 발행 (비동기 고려 가능하지만, Phase 0에서는 동기 처리하고 추후 리팩토링 하기로)
        if (StringUtils.hasText(condition.keyword())) {
            // 최근 검색어 저장 (Async)
            if (userId != null) {
                recentSearchService.addKeyword(userId, condition.keyword());
            }
            publishSearchEvent(userId, condition, result);
        }

        return result;
    }

    private void publishSearchEvent(Long userId, SearchCondition condition, SearchResultPage<SearchAuctionItemView> result) {
        eventPublisher.publishEvent(new SearchAuctionItemEvent(
                userId,
                condition.keyword(),
                condition.categories() != null ? new ArrayList<>(condition.categories()) : new ArrayList<>(),
                condition.searchPriceRange() != null ? condition.searchPriceRange().min() : null,
                condition.searchPriceRange() != null ? condition.searchPriceRange().max() : null,
                condition.statuses() != null ? new ArrayList<>(condition.statuses()) : new ArrayList<>(),
                (int) result.totalElements(),
                result.totalElements() > 0,
                LocalDateTime.now()
        ));
    }
}
