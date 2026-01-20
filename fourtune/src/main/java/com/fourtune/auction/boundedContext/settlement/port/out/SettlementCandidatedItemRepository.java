package com.fourtune.auction.boundedContext.settlement.port.out;

import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementCandidatedItemRepository extends JpaRepository<SettlementCandidatedItem, Long> {
}
