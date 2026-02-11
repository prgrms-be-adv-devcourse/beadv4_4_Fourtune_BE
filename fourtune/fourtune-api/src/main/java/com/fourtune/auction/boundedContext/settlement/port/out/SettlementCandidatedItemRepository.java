package com.fourtune.auction.boundedContext.settlement.port.out;

import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementItem;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementCandidatedItemRepository extends JpaRepository<SettlementCandidatedItem, Long> {
    List<SettlementCandidatedItem> findBySettlementItemIsNullAndPaymentDateIsBeforeOrderByPayeeAscIdAsc(LocalDateTime minDate, PageRequest of);

    List<SettlementCandidatedItem> findByPayee_Id(Long payeeId);
}
