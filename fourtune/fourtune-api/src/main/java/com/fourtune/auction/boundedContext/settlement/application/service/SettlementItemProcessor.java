package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개별 정산 건 처리. REQUIRES_NEW로 한 건 실패가 전체 Chunk 롤백되지 않도록 격리.
 */
@Component
@RequiredArgsConstructor
public class SettlementItemProcessor {

    private final SettlementRepository settlementRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + settlementId));
        settlement.competeSettlement();
    }
}
