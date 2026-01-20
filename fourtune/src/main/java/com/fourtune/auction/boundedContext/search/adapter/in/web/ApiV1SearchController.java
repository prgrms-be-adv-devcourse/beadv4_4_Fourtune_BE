package com.fourtune.auction.boundedContext.search.adapter.in.web;

import com.fourtune.auction.boundedContext.search.application.service.SearchFacade;
import com.fourtune.auction.boundedContext.search.domain.SearchPriceRange;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.auction.boundedContext.search.domain.constant.SearchSort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class ApiV1SearchController {

    private final SearchFacade facade;

    @GetMapping("/auction-items")
    public SearchResultPage<SearchAuctionItemView> searchAuctionItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Set<String> category,
            @RequestParam(required = false) Set<String> status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "LATEST") SearchSort sort,
            @RequestParam(defaultValue = "1") int page
    ) {
        SearchCondition condition = new SearchCondition(
                keyword,
                category,
                new SearchPriceRange(minPrice, maxPrice),
                status,
                sort,
                page
        );
        return facade.search(condition);
    }
}