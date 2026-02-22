package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SettlementCompleteChunkUseCase {

    private final SettlementRepository settlementRepository;

    public int completeSettlementsChunk(int size) {
        List<Settlement> activeSettlements = findActiveSettlements(size);

        if (activeSettlements.isEmpty())
            return 0;

        activeSettlements.forEach(Settlement::competeSettlement);

        return activeSettlements.size();
    }

    private List<Settlement> findActiveSettlements(int size) {
        // 정산 일시(SettledAt)가 아직 Null이고(정산 안 됐고), 금액이 0보다 큰 것 조회
        return settlementRepository.findBySettledAtIsNullAndAmountGreaterThanOrderByIdAsc(
                0L, PageRequest.of(0, size)
                );
    }
}
