package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
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
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ElasticsearchAuctionItemSearchEngine implements AuctionItemSearchEngine {

    private final ElasticsearchOperations operations;
    private final SearchProperties props;

    // 검색 필터로 허용할 상태(요구사항 3개만)
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            AuctionStatus.SCHEDULED.name(),
            AuctionStatus.ACTIVE.name(),
            AuctionStatus.ENDED.name()
    );

    @Override
    public SearchResultPage<SearchAuctionItemView> search(SearchCondition condition) {

        int size = props.getPageSize();          // 기본 20, yml로 변경 가능
        int page = condition.safePage();         // 1부터
        int from = (page - 1) * size;

        // from/size deep paging 가드 (추후 search_after 전환 포인트)
        if (from > props.getMaxFrom()) {
            throw new IllegalArgumentException("Too deep paging: from=" + from + ", maxFrom=" + props.getMaxFrom());
        }

        CriteriaQuery query = new CriteriaQuery(buildCriteria(condition));
        query.setPageable(PageRequest.of(page - 1, size));
        query.addSort(buildSort(condition.sort()));

        SearchHits<SearchAuctionItemDocument> hits = operations.search(query, SearchAuctionItemDocument.class);

        var items = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toView)
                .toList();

        long total = hits.getTotalHits();
        boolean hasNext = (long) from + items.size() < total;

        return new SearchResultPage<>(items, total, page, size, hasNext);
    }

    private Criteria buildCriteria(SearchCondition c) {
        Criteria root = new Criteria();

        // 1) 키워드: title OR description
        if (hasText(c.keyword())) {
            String keyword = c.keyword().trim();

            // matches = full-text
            Criteria keywordCriteria =
                    new Criteria("title").matches(keyword)
                            .or(new Criteria("description").matches(keyword));

            root = root.and(keywordCriteria);
        }

        // 2) 카테고리 필터 (정본 enum name 문자열만 허용)
        Set<String> categories = normalizeEnumNames(c.categories());
        categories = filterAllowedCategories(categories);
        if (!categories.isEmpty()) {
            root = root.and(new Criteria("category").in(categories));
        }

        // 3) 상태 필터 (요구사항 3개만 허용)
        Set<String> statuses = normalizeEnumNames(c.statuses());
        statuses = statuses.stream()
                .filter(ALLOWED_STATUSES::contains)
                .collect(Collectors.toSet());
        if (!statuses.isEmpty()) {
            root = root.and(new Criteria("status").in(statuses));
        }

        // 4) 가격 범위 필터 (currentPrice)
        SearchPriceRange pr = c.searchPriceRange();
        if (pr != null && !pr.isEmpty()) {
            Criteria price = new Criteria("currentPrice");
            if (pr.min() != null) price = price.greaterThanEqual(pr.min());
            if (pr.max() != null) price = price.lessThanEqual(pr.max());
            root = root.and(price);
        }

        return root;
    }

    private Sort buildSort(SearchSort sort) {
        SearchSort s = (sort != null) ? sort : SearchSort.LATEST;

        // 최신순: createdAt desc
        if (s == SearchSort.LATEST) {
            return Sort.by(Sort.Order.desc("createdAt"));
        }

        // 인기순(MVP): viewCount desc, tie-break createdAt desc
        if (s == SearchSort.POPULAR) {
            return Sort.by(
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("createdAt")
            );
        }

        return Sort.by(Sort.Order.desc("createdAt"));
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
                d.getStartAt(),
                d.getEndAt(),
                d.getThumbnailUrl(),
                d.getCreatedAt(),
                d.getUpdatedAt(),
                d.getViewCount(),
                d.getWatchlistCount(),
                d.getBidCount()
        );
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private Set<String> normalizeEnumNames(Set<String> raw) {
        if (raw == null) return Set.of();
        return raw.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private Set<String> filterAllowedCategories(Set<String> normalized) {
        if (normalized.isEmpty()) return Set.of();
        Set<String> allowed = Set.of(
                Category.ELECTRONICS.name(),
                Category.CLOTHING.name(),
                Category.POTTERY.name(),
                Category.APPLIANCES.name(),
                Category.BEDDING.name(),
                Category.BOOKS.name(),
                Category.COLLECTIBLES.name(),
                Category.ETC.name()
        );
        return normalized.stream()
                .filter(allowed::contains)
                .collect(Collectors.toSet());
    }
}
