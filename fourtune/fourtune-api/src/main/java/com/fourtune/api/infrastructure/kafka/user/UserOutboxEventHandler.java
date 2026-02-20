package com.fourtune.api.infrastructure.kafka.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.core.outbox.handler.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * User 도메인 Outbox 이벤트 핸들러
 * payload = {"eventType":"...","aggregateId":123,"data":{...}} 형태로 저장된 JSON을 파싱 후 Kafka 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class UserOutboxEventHandler implements OutboxEventHandler {

    private static final String AGGREGATE_TYPE = "User";

    private final UserKafkaProducer userKafkaProducer;
    private final ObjectMapper objectMapper;

    @Override
    public String getAggregateType() {
        return AGGREGATE_TYPE;
    }

    @Override
    public void handle(String payload) throws Exception {
        UserEventPayload wrapper = objectMapper.readValue(payload, UserEventPayload.class);
        String key = String.valueOf(wrapper.getAggregateId());
        String eventType = wrapper.getEventType();
        String value = objectMapper.writeValueAsString(wrapper.getData());
        userKafkaProducer.sendSync(key, value, eventType);
    }
}
