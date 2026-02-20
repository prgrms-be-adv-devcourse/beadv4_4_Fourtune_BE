package com.fourtune.auction.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.core.outbox.handler.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 경매 도메인 Outbox 이벤트 핸들러
 * payload = {"eventType":"...","aggregateId":123,"data":{...}} 형태로 저장된 JSON을 파싱 후 Kafka 발행
 * (User/Outbox 인터페이스 변경 없이 handle(String payload)만 사용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class AuctionOutboxEventHandler implements OutboxEventHandler {

    private static final String AGGREGATE_TYPE = "Auction";

    private final AuctionKafkaProducer auctionKafkaProducer;
    private final ObjectMapper objectMapper;

    @Override
    public String getAggregateType() {
        return AGGREGATE_TYPE;
    }

    @Override
    public void handle(String payload) throws Exception {
        AuctionEventPayload wrapper = objectMapper.readValue(payload, AuctionEventPayload.class);
        String key = String.valueOf(wrapper.getAggregateId());
        String eventType = wrapper.getEventType();
        String value = objectMapper.writeValueAsString(wrapper.getData());
        auctionKafkaProducer.sendSync(key, value, eventType);
    }
}
