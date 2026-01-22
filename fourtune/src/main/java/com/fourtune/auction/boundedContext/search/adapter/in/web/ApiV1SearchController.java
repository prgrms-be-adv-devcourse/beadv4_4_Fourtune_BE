package com.fourtune.auction.boundedContext.search.adapter.in.web;

import com.fourtune.auction.boundedContext.search.application.service.SearchFacade;
import com.fourtune.auction.boundedContext.search.domain.SearchPriceRange;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.auction.boundedContext.search.domain.constant.SearchSort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class ApiV1SearchController {

    private final SearchFacade facade;

    @GetMapping("/auction-items")
    public ResponseEntity<SearchResultPage<SearchAuctionItemView>> searchAuctionItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Set<String> categories,  // enum name 문자열들
            @RequestParam(required = false) Set<String> statuses,    // "SCHEDULED", "ACTIVE", "ENDED" 등
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "LATEST") SearchSort sort,
            @RequestParam(defaultValue = "1") int page              // 1부터 받을 예정
    ) {
        SearchCondition condition = new SearchCondition(
                keyword,
                categories,
                new SearchPriceRange(minPrice, maxPrice),
                statuses,
                sort,
                page
        );

        return ResponseEntity.ok(facade.search(condition));
    }
}
