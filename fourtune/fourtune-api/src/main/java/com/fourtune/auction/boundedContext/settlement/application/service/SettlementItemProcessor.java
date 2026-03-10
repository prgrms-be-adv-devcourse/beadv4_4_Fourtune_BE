package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementRepository;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.shared.kafka.settlement.SettlementEventMapper;
import com.fourtune.shared.settlement.event.SettlementCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 개별 정산 건 처리. REQUIRES_NEW로 한 건 실패가 전체 Chunk 롤백되지 않도록 격리.
 */
@Component
@RequiredArgsConstructor
public class SettlementItemProcessor {

    private static final String AGGREGATE_TYPE = "Settlement";

    private final SettlementRepository settlementRepository;
    private final OutboxService outboxService;
    private final EventPublishingConfig eventPublishingConfig;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + settlementId));

        settlement.completeSettlement();

        if (eventPublishingConfig.isKafkaEnabled()) {
            SettlementCompletedEvent event = new SettlementCompletedEvent(settlement.toDto());
            outboxService.append(
                    AGGREGATE_TYPE,
                    settlement.getId(),
                    SettlementEventMapper.EventType.SETTLEMENT_COMPLETED.name(),
                    Map.of(
                            "eventType", SettlementEventMapper.EventType.SETTLEMENT_COMPLETED.name(),
                            "aggregateId", settlement.getId(),
                            "data", event
                    )
            );
        }
    }
}
