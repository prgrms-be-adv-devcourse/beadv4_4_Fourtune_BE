package com.fourtune.common.shared.payment.kafka;

import com.fourtune.common.global.config.kafka.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 결제 이벤트 Kafka Producer
 * 파티션 키로 orderId(aggregateId)를 사용하여 같은 주문 내 이벤트 순서 보장
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentKafkaProducer {

    private static final String HEADER_EVENT_TYPE = "X-Event-Type";

    private final KafkaTemplate<String, String> paymentKafkaTemplate;

    /**
     * 결제 이벤트 발행 (payload = JSON 문자열, Header에 X-Event-Type 포함)
     */
    public CompletableFuture<?> send(String key, String payload, String eventType) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopicConfig.PAYMENT_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(HEADER_EVENT_TYPE, eventType)
                .build();
        return paymentKafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Payment 이벤트 발행 성공: topic={}, key={}, eventType={}",
                                KafkaTopicConfig.PAYMENT_EVENTS_TOPIC, key, eventType);
                    } else {
                        log.error("Payment 이벤트 발행 실패: topic={}, key={}, eventType={}, error={}",
                                KafkaTopicConfig.PAYMENT_EVENTS_TOPIC, key, eventType, ex.getMessage(), ex);
                    }
                });
    }

    /**
     * 동기 발행 (OutboxPublisher에서 사용)
     */
    public void sendSync(String key, String payload, String eventType) {
        try {
            send(key, payload, eventType).get();
        } catch (Exception e) {
            log.error("Payment 이벤트 동기 발행 실패: key={}, eventType={}", key, eventType, e);
            throw new RuntimeException("Payment Kafka 메시지 발행 실패", e);
        }
    }
}