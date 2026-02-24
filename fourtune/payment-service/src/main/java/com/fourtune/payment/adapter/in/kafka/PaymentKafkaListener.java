package com.fourtune.payment.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.payment.application.service.PaymentFacade;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.shared.kafka.payment.PaymentEventMapper;
import com.fourtune.shared.payment.event.PaymentUserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Payment 도메인 내부 이벤트 처리
 * (PaymentUserCreated 등 내부 이벤트를 Kafka를 통해 처리)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentKafkaListener {

    private static final String HEADER_EVENT_TYPE = "X-Event-Type";

    private final ObjectMapper objectMapper;
    private final PaymentFacade paymentFacade;

    @KafkaListener(
            topics = KafkaTopicConfig.PAYMENT_EVENTS_TOPIC,
            groupId = "payment-events-group",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(
            @Payload String payload,
            @Header(HEADER_EVENT_TYPE) String eventType,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        log.info("Payment 내부 이벤트 수신: eventType={}, key={}", eventType, key);

        try {
            // 1. eventType으로 Class 찾기
            Class<?> eventClass = PaymentEventMapper.getClass(eventType);

            // 2. JSON → 이벤트 객체 역직렬화
            Object event = objectMapper.readValue(payload, eventClass);

            // 3. 이벤트 타입별 처리
            switch (PaymentEventMapper.EventType.valueOf(eventType)) {
                case PAYMENT_USER_CREATED -> handlePaymentUserCreated((PaymentUserCreatedEvent) event);
                case PAYMENT_SUCCEEDED -> log.debug("PAYMENT_SUCCEEDED 이벤트 무시 (내부 처리 불필요)");
                case PAYMENT_FAILED -> log.debug("PAYMENT_FAILED 이벤트 무시 (내부 처리 불필요)");
                case PAYMENT_CANCELED -> log.debug("PAYMENT_CANCELED 이벤트 무시 (내부 처리 불필요)");
                case AUCTION_REFUND_COMPLETED -> log.debug("AUCTION_REFUND_COMPLETED 이벤트 무시 (내부 처리 불필요)");
            }

        } catch (Exception e) {
            log.error("Payment 이벤트 처리 실패: eventType={}, key={}, payload={}",
                    eventType, key, payload, e);
            throw new RuntimeException("Payment 이벤트 처리 실패", e);
        }
    }

    /**
     * 결제 사용자 생성 → 지갑 생성
     */
    private void handlePaymentUserCreated(PaymentUserCreatedEvent event) {
        log.info("결제 사용자 생성 이벤트 처리 시작: userId={}",
                event.getPaymentUserDto().getId());

        try {
            paymentFacade.createWallet(event.getPaymentUserDto());
            log.info("지갑 생성 완료: userId={}", event.getPaymentUserDto().getId());
        } catch (Exception e) {
            log.error("지갑 생성 실패: userId={}", event.getPaymentUserDto().getId(), e);
            throw e;
        }
    }
}