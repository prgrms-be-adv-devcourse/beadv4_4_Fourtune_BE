package com.fourtune.auction.boundedContext.search.adapter.in.event;

import com.fourtune.auction.boundedContext.search.domain.SearchLog;
import com.fourtune.auction.boundedContext.search.port.out.SearchLogRepository;
import com.fourtune.auction.shared.search.event.SearchAuctionItemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLogEventListener {

    private final SearchLogRepository searchLogRepository;

    @Async("taskExecutor") // AsyncConfig에서 정의한 스레드 풀 사용
    @EventListener
    @Transactional
    public void handleSearchEvent(SearchAuctionItemEvent event) {
        log.info("[SEARCH][LOG] 비동기 검색 로그 저장 시작 - keyword: {}, userId: {}", event.keyword(), event.userId());

        try {
            SearchLog logEntity = SearchLog.builder()
                    .userId(event.userId())
                    .keyword(event.keyword())
                    .categories(event.categories() != null ? event.categories() : new ArrayList<>())
                    .minPrice(event.minPrice() != null ? event.minPrice() : BigDecimal.ZERO)
                    .maxPrice(event.maxPrice())
                    .status(event.status() != null ? event.status() : new ArrayList<>())
                    .resultCount(event.resultCount())
                    .isSuccess(event.isSuccess())
                    .build();

            searchLogRepository.save(logEntity);
            log.debug("[SEARCH][LOG] 비동기 검색 로그 저장 완료 - keyword: {}, resultCount: {}", event.keyword(), event.resultCount());

        } catch (Exception e) {
            log.error("[SEARCH][LOG] 비동기 검색 로그 저장 실패 - keyword: {}", event.keyword(), e);
            // 비동기 처리 중 에러 발생 시 메인 로직에는 영향 없음
        }
    }
}
