package com.fourtune.auction.boundedContext.search.adapter.in.web;

import com.fourtune.auction.boundedContext.search.application.service.SearchFacade;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.SearchPriceRange;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.auction.boundedContext.search.domain.constant.SearchSort;
import com.fourtune.common.shared.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal UserContext user,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Set<String> categories,
            @RequestParam(required = false) Set<String> statuses,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "LATEST") SearchSort sort,
            @RequestParam(defaultValue = "1") int page) {
        SearchCondition condition = new SearchCondition(
                keyword,
                categories,
                new SearchPriceRange(minPrice, maxPrice),
                statuses,
                sort,
                page);

        Long userId = (user != null) ? user.id() : null;
        return ResponseEntity.ok(facade.search(userId, condition));
    }
}
