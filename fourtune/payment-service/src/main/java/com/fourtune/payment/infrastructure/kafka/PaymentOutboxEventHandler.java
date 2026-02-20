package com.fourtune.payment.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.outbox.handler.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 결제 도메인 Outbox 이벤트 핸들러
 * payload = {"eventType":"...","aggregateId":123,"data":{...}} 형태로 저장된 JSON을 파싱
 * 후 Kafka 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentOutboxEventHandler implements OutboxEventHandler {

    private static final String AGGREGATE_TYPE = "Payment";

    private final PaymentKafkaProducer paymentKafkaProducer;
    private final ObjectMapper objectMapper;

    @Override
    public String getAggregateType() {
        return AGGREGATE_TYPE;
    }

    @Override
    public void handle(String payload) throws Exception {
        PaymentEventPayload wrapper = objectMapper.readValue(payload, PaymentEventPayload.class);
        String key = String.valueOf(wrapper.getAggregateId());
        String eventType = wrapper.getEventType();
        String value = objectMapper.writeValueAsString(wrapper.getData());
        paymentKafkaProducer.sendSync(key, value, eventType);
    }
}