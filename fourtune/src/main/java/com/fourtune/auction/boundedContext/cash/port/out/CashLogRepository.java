package com.fourtune.auction.boundedContext.cash.port.out;

import com.fourtune.auction.boundedContext.cash.domain.entity.CashLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashLogRepository extends JpaRepository<CashLog, Long> {
}
