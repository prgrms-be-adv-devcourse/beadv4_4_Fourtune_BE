package com.fourtune.auction.boundedContext.settlement.port.out;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findAllByPayee(SettlementUser payee);

    List<Settlement> findFirstByPayeeIdAndSettledAtIsNotNullOrderByCreatedAtDesc(Long payeeId);
}
