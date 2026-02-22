package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettlementCompleteChunkUseCase {

    private final SettlementRepository settlementRepository;
    private final SettlementItemProcessor settlementItemProcessor;

    /**
     * 정산 Chunk 처리. 개별 건마다 REQUIRES_NEW + try-catch로 실패 시에도 나머지 건 계속 처리.
     */
    public int completeSettlementsChunk(int size) {
        List<Settlement> activeSettlements = findActiveSettlements(size);

        if (activeSettlements.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Settlement settlement : activeSettlements) {
            try {
                settlementItemProcessor.processOne(settlement.getId());
                successCount++;
            } catch (Exception e) {
                log.warn("정산 1건 처리 실패(다음 건 계속): settlementId={}, error={}", settlement.getId(), e.getMessage());
            }
        }
        return successCount;
    }

    private List<Settlement> findActiveSettlements(int size) {
        return settlementRepository.findBySettledAtIsNullAndAmountGreaterThanOrderByIdAsc(
                0L, PageRequest.of(0, size));
    }
}
