package com.fourtune.recommendation.adapter.out.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

/**
 * fourtune-api 검색 API 호출용 Feign 클라이언트.
 * GET /api/v1/search/auction-items 로 카테고리 기반 상품 검색을 수행합니다.
 */
@FeignClient(name = "search-service", url = "${api.search.base-url}", configuration = RecommendationFeignConfig.class)
public interface SearchClient {

    /**
     * 카테고리 + 상태 기반 경매 상품 검색.
     * 응답 구조: { items: [...], totalElements, page, size, hasNext }
     */
    @GetMapping("/api/v1/search/auction-items")
    Map<String, Object> searchAuctionItems(
            @RequestParam(value = "categories", required = false) Set<String> categories,
            @RequestParam(value = "statuses", required = false) Set<String> statuses,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", required = false) Integer page);
}
