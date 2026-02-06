package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch;

import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.document.SearchAuctionItemDocument;
import com.fourtune.auction.boundedContext.search.application.service.AuctionItemSearchEngine;
import com.fourtune.auction.boundedContext.search.application.service.SearchProperties;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.SearchPriceRange;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.auction.boundedContext.search.domain.constant.SearchSort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch._types.SortOptions;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchAuctionItemSearchEngine implements AuctionItemSearchEngine {

    private final ElasticsearchOperations operations;
    private final SearchProperties props;

    // 검색 필터로 허용할 상태(요구사항 3개만)
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "SCHEDULED", "ACTIVE", "ENDED");

    // 카테고리 필터로 허용할 카테고리
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "ELECTRONICS", "CLOTHING", "POTTERY", "APPLIANCES", "BEDDING", "BOOKS", "COLLECTIBLES", "ETC");

    @Override
    public SearchResultPage<SearchAuctionItemView> search(SearchCondition condition) {

        int size = props.getPageSize();
        int page = condition.safePage();
        int from = (page - 1) * size;

        if (from > props.getMaxFrom()) {
            throw new IllegalArgumentException("Too deep paging: from=" + from + ", maxFrom=" + props.getMaxFrom());
        }

        // NativeQuery 생성
        Query query = buildNativeQuery(condition);

        var nativeQueryBuilder = NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(page - 1, size));

        if (condition.sort() == SearchSort.POPULAR) {
            nativeQueryBuilder.withSort(buildPopularScriptSort());
        } else {
            nativeQueryBuilder.withSort(buildSort(condition.sort()));
        }

        NativeQuery nativeQuery = nativeQueryBuilder.build();

        SearchHits<SearchAuctionItemDocument> hits = operations.search(nativeQuery, SearchAuctionItemDocument.class);

        var items = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toView)
                .toList();

        long total = hits.getTotalHits();
        boolean hasNext = (long) from + items.size() < total;

        return new SearchResultPage<>(items, total, page, size, hasNext);
    }

    private Query buildNativeQuery(SearchCondition c) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 1) 키워드: title OR description (Unified via 'should' inside a 'must' or
        // directly if it's the only constraint)
        // 하지만 여기서는 다른 필터랑 결합되므로 boolQuery의 'must' 절 안에 'should'를 넣거나,
        // 전체 boolQuery의 구조를 잡아야 함.
        // Elastic 8.x+ Java API Client 스타일:

        if (hasText(c.keyword())) {
            String keyword = c.keyword().trim();

            // (title contains keyword OR description contains keyword)
            // matchQuery 대신 wildcard나 query_string을 고려할 수도 있으나, 여기서는 match(contains 의미) 시도
            Query titleQuery = MatchQuery.of(m -> m.field("title").query(keyword))._toQuery();
            Query descQuery = MatchQuery.of(m -> m.field("description").query(keyword))._toQuery();

            boolQueryBuilder.must(m -> m.bool(b -> b.should(titleQuery).should(descQuery)));
        }

        // 2) 카테고리 필터
        Set<String> categories = normalizeEnumNames(c.categories());
        categories = filterAllowedCategories(categories);
        if (!categories.isEmpty()) {
            // terms query
            List<FieldValue> categoryValues = categories.stream().map(FieldValue::of).toList();
            boolQueryBuilder.filter(f -> f.terms(t -> t.field("category").terms(ts -> ts.value(categoryValues))));
        }

        // 3) 상태 필터
        Set<String> statuses = normalizeEnumNames(c.statuses());
        statuses = statuses.stream()
                .filter(ALLOWED_STATUSES::contains)
                .collect(Collectors.toSet());
        if (!statuses.isEmpty()) {
            List<FieldValue> statusValues = statuses.stream().map(FieldValue::of).toList();
            boolQueryBuilder.filter(f -> f.terms(t -> t.field("status").terms(ts -> ts.value(statusValues))));
        }

        // 4) 가격 범위 필터
        SearchPriceRange pr = c.searchPriceRange();
        if (pr != null && !pr.isEmpty()) {
            boolQueryBuilder.filter(f -> f.range(r -> r
                    .number(n -> {
                        n.field("currentPrice");
                        if (pr.min() != null)
                            n.gte(pr.min().doubleValue());
                        if (pr.max() != null)
                            n.lte(pr.max().doubleValue());
                        return n;
                    })));
        }

        return boolQueryBuilder.build()._toQuery();
    }

    private Sort buildSort(SearchSort sort) {
        SearchSort s = (sort != null) ? sort : SearchSort.LATEST;

        // 최신순: createdAt desc
        if (s == SearchSort.LATEST) {
            return Sort.by(Sort.Order.desc("createdAt"));
        }

        // 마감임박순: endAt asc (종료 시간 빠른 순)
        if (s == SearchSort.ENDS_SOON) {
            return Sort.by(
                    Sort.Order.asc("endAt"),
                    Sort.Order.desc("createdAt")); // 시간 같으면 최신순
        }

        return Sort.by(Sort.Order.desc("createdAt"));
    }

    private SortOptions buildPopularScriptSort() {
        // [신선도 가중 인기순 정렬 공식]
        // 최종 점수 = 활동 점수 (Base) / 신선도 감가 (Decay)
        String scriptCode = """
            long now = params.now;
            long createdAt = doc['createdAt'].value.toInstant().toEpochMilli();
            double hoursOld = (now - createdAt) / 3600000.0;
            double viewVal = (doc['viewCount'].size() > 0) ? doc['viewCount'].value : 0;
            double viewScore = Math.log10(viewVal + 1);
            double watchVal = (doc['watchlistCount'].size() > 0) ? doc['watchlistCount'].value : 0;
            double bidVal = (doc['bidCount'].size() > 0) ? doc['bidCount'].value : 0;
            double baseScore = (viewScore * 1.0) + (watchVal * 3.0) + (bidVal * 5.0);
            double decay = Math.pow(hoursOld + 2, 0.5);
            return baseScore / decay;
            """;
            
        // Escape script for JSON
        String escapedScript = scriptCode.replace("\n", " ").replace("\"", "\\\"");
        long nowParam = System.currentTimeMillis();

        String json = String.format("""
            {
                "_script": {
                    "type": "number",
                    "script": {
                        "source": "%s",
                        "lang": "painless",
                        "params": {
                            "now": %d
                        }
                    },
                    "order": "desc"
                }
            }
            """, escapedScript, nowParam);

        return co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                .withJson(new StringReader(json))
        );
    }



    private SearchAuctionItemView toView(SearchAuctionItemDocument d) {
        return new SearchAuctionItemView(
                d.getAuctionItemId(),
                d.getTitle(),
                d.getDescription(),
                d.getCategory(),
                d.getStatus(),
                d.getStartPrice(),
                d.getCurrentPrice(),
                d.getBuyNowPrice(),
                d.getBuyNowEnabled(),
                d.getStartAt(),
                d.getEndAt(),
                d.getThumbnailUrl(),
                d.getCreatedAt(),
                d.getUpdatedAt(),
                d.getViewCount(),
                d.getWatchlistCount(),
                d.getBidCount());
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private Set<String> normalizeEnumNames(Set<String> raw) {
        if (raw == null)
            return Set.of();
        return raw.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private Set<String> filterAllowedCategories(Set<String> normalized) {
        return normalized.stream()
                .filter(ALLOWED_CATEGORIES::contains)
                .collect(Collectors.toSet());
    }
}
