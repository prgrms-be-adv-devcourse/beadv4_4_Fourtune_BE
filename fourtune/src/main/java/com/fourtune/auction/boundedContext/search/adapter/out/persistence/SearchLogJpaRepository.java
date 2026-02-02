package com.fourtune.auction.boundedContext.search.adapter.out.persistence;

import com.fourtune.auction.boundedContext.search.domain.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogJpaRepository extends JpaRepository<SearchLog, Long> {
}
