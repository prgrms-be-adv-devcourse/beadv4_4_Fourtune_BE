package com.fourtune.auction.boundedContext.search.adapter.in.web;

import com.fourtune.auction.boundedContext.search.application.service.SearchFacade;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;

import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.auction.boundedContext.search.domain.constant.SearchSort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 내부 서버 간 통신을 위한 Search API 컨트롤러.
 * API Gateway나 Spring Security를 통해 외부 클라이언트의 직접 접근을 차단해야 합니다.
 * 추천 서비스 등이 크기가 큰 후보군 데이터를 조회할 때 사용됩니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/v1/search")
public class InternalApiV1SearchController {

    private final SearchFacade facade;

    @GetMapping("/candidates")
    public ResponseEntity<SearchResultPage<SearchAuctionItemView>> getSearchCandidates(
            @RequestParam(required = false) Set<String> categories,
            @RequestParam(required = false) Set<String> statuses,
            @RequestParam(defaultValue = "POPULAR") SearchSort sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") Integer size) {

        SearchCondition condition = new SearchCondition(
                null, // 키워드
                categories,
                null, // priceRange
                statuses,
                sort,
                page,
                size);

        // 내부 API이므로 User ID(로깅용 등)는 null로 검색 실행
        return ResponseEntity.ok(facade.search(null, condition));
    }
}
