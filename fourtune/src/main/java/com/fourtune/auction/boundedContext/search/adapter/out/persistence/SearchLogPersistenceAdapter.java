package com.fourtune.auction.boundedContext.search.adapter.out.persistence;

import com.fourtune.auction.boundedContext.search.domain.SearchLog;
import com.fourtune.auction.boundedContext.search.port.out.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchLogPersistenceAdapter implements SearchLogRepository {

    private final SearchLogJpaRepository jpaRepository;

    @Override
    public void save(SearchLog searchLog) {
        jpaRepository.save(searchLog);
    }
}
