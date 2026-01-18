package com.fourtune.auction.boundedContext.settlement.port.out;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
