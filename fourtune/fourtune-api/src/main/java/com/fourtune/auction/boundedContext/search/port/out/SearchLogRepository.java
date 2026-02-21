package com.fourtune.auction.boundedContext.search.port.out;

import com.fourtune.auction.boundedContext.search.domain.SearchLog;

public interface SearchLogRepository {
    void save(SearchLog searchLog);
}
