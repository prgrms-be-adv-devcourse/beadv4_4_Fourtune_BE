package com.fourtune.auction.boundedContext.payment.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.common.global.config.kafka.KafkaTopicConfig;
import com.fourtune.common.shared.settlement.event.SettlementCompletedEvent;
import com.fourtune.common.shared.settlement.kafka.SettlementEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Settlement 이벤트를 구독하여 지급 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentSettlementKafkaListener {

    private static final String HEADER_EVENT_TYPE = "X-Event-Type";

    private final ObjectMapper objectMapper;
    private final PaymentFacade paymentFacade;

    @KafkaListener(
            topics = KafkaTopicConfig.PAYMENT_EVENTS_TOPIC,
            groupId = "${payment-settlement-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleSettlementEvent(
            @Payload String payload,
            @Header(HEADER_EVENT_TYPE) String eventType,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        log.info("Settlement 이벤트 수신: eventType={}, key={}", eventType, key);

        try {
            // 1. eventType으로 Class 찾기
            Class<?> eventClass = SettlementEventMapper.getClass(eventType);

            // 2. JSON → 이벤트 객체 역직렬화
            Object event = objectMapper.readValue(payload, eventClass);

            // 3. 이벤트 타입별 처리
            switch (SettlementEventMapper.EventType.valueOf(eventType)) {
                case SETTLEMENT_COMPLETED -> handleSettlementCompleted((SettlementCompletedEvent) event);
                case SETTLEMENT_USER_CREATED -> log.debug("SETTLEMENT_USER_CREATED 이벤트 무시");
            }

        } catch (Exception e) {
            log.error("Settlement 이벤트 처리 실패: eventType={}, key={}, payload={}",
                    eventType, key, payload, e);
            throw new RuntimeException("Settlement 이벤트 처리 실패", e);
        }
    }

    /**
     * 정산 완료 → 지급
     */
    private void handleSettlementCompleted(SettlementCompletedEvent event) {
        log.info("정산 완료 이벤트 처리 시작: settlementId={}, userId={}, amount={}");

        try {
            paymentFacade.completeSettlement(event.getSettlementDto());
        } catch (Exception e) {
            throw e;
        }
    }


}