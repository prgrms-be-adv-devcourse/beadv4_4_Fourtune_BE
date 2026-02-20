package com.fourtune.kafka.watchList;

import com.fourtune.kafka.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * WatchList 이벤트 Kafka Producer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class WatchListKafkaProducer {

    private static final String HEADER_EVENT_TYPE = "X-Event-Type";

    private final KafkaTemplate<String, String> auctionKafkaTemplate;

    public void send(String key, String payload, String eventType) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopicConfig.WATCHLIST_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(HEADER_EVENT_TYPE, eventType)
                .build();
        auctionKafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("WatchList 이벤트 발행 성공: key={}, eventType={}", key, eventType);
                    } else {
                        log.error("WatchList 이벤트 발행 실패: key={}, eventType={}, error={}",
                                key, eventType, ex.getMessage(), ex);
                    }
                });
    }
}
