package com.fourtune.auction.boundedContext.settlement.port.out;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findAllByPayee(SettlementUser payee);

    List<Settlement> findFirstByPayeeIdAndSettledAtIsNotNullOrderByCreatedAtDesc(Long payeeId);

    Optional<Settlement> findByPayeeAndSettledAtIsNull(SettlementUser payee);

    List<Settlement> findBySettledAtIsNullAndAmountGreaterThanOrderByIdAsc(Long amount, PageRequest of);
}
