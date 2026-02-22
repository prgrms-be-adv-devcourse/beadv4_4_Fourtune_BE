package com.fourtune.auction.boundedContext.settlement.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.settlement.application.service.SettlementFacade;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.shared.settlement.event.SettlementCompletedEvent;
import com.fourtune.shared.settlement.event.SettlementUserCreatedEvent;
import com.fourtune.shared.kafka.settlement.SettlementEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Settlement 도메인 내부 이벤트 처리
 * (SettlementUserCreated, SettlementCompleted 등 내부 이벤트를 Kafka를 통해 처리)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class SettlementKafkaListener {

    private static final String HEADER_EVENT_TYPE = "X-Event-Type";

    private final ObjectMapper objectMapper;
    private final SettlementFacade settlementFacade;

    @KafkaListener(
            topics = KafkaTopicConfig.SETTLEMENT_EVENTS_TOPIC,
            groupId = "settlement-events-group",
            containerFactory = "settlementEventKafkaListenerContainerFactory"
    )
    public void handleSettlementEvent(
            @Payload String payload,
            @Header(HEADER_EVENT_TYPE) String eventType,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        log.info("Settlement 내부 이벤트 수신: eventType={}, key={}", eventType, key);

        try {
            // 1. eventType으로 Class 찾기
            Class<?> eventClass = SettlementEventMapper.getClass(eventType);

            // 2. JSON → 이벤트 객체 역직렬화
            Object event = objectMapper.readValue(payload, eventClass);

            // 3. 이벤트 타입별 처리
            switch (SettlementEventMapper.EventType.valueOf(eventType)) {
                case SETTLEMENT_USER_CREATED -> handleSettlementUserCreated((SettlementUserCreatedEvent) event);
                case SETTLEMENT_COMPLETED -> handleSettlementCompleted((SettlementCompletedEvent) event);
            }

        } catch (Exception e) {
            log.error("Settlement 이벤트 처리 실패: eventType={}, key={}, payload={}",
                    eventType, key, payload, e);
            throw new RuntimeException("Settlement 이벤트 처리 실패", e);
        }
    }

    /**
     * 정산 유저 생성 → 초기 정산서 생성
     */
    private void handleSettlementUserCreated(SettlementUserCreatedEvent event) {
        log.info("[SettlementListener] 정산 유저 생성 확인 -> 초기 정산서 생성: userId={}",
                event.getSettlementUserDto().getId());

        try {
            settlementFacade.createSettlement(event.getSettlementUserDto().getId());
            log.info("초기 정산서 생성 완료: userId={}", event.getSettlementUserDto().getId());
        } catch (Exception e) {
            log.error("초기 정산서 생성 실패: userId={}", event.getSettlementUserDto().getId(), e);
            throw e;
        }
    }

    /**
     * 정산 완료 → 새로운 정산서 생성
     */
    private void handleSettlementCompleted(SettlementCompletedEvent event) {
        log.info("[SettlementListener] 정산 완료 확인 -> 새 정산서 생성: payeeId={}",
                event.getSettlementDto().getPayeeId());

        try {
            settlementFacade.createSettlement(event.getSettlementDto().getPayeeId());
            log.info("새 정산서 생성 완료: payeeId={}", event.getSettlementDto().getPayeeId());
        } catch (Exception e) {
            log.error("새 정산서 생성 실패: payeeId={}", event.getSettlementDto().getPayeeId(), e);
            throw e;
        }
    }
}