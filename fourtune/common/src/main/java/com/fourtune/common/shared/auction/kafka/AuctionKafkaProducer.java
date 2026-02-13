package com.fourtune.common.shared.auction.kafka;

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
 * 경매 이벤트 Kafka Producer
 * 파티션 키로 auctionId(aggregateId)를 사용하여 같은 경매 내 이벤트 순서 보장
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class AuctionKafkaProducer {

    private static final String HEADER_EVENT_TYPE = "X-Event-Type";

    private final KafkaTemplate<String, String> auctionKafkaTemplate;

    /**
     * 경매 이벤트 발행 (payload = JSON 문자열, Header에 X-Event-Type 포함)
     */
    public CompletableFuture<?> send(String key, String payload, String eventType) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopicConfig.AUCTION_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(HEADER_EVENT_TYPE, eventType)
                .build();
        return auctionKafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Auction 이벤트 발행 성공: topic={}, key={}, eventType={}",
                                KafkaTopicConfig.AUCTION_EVENTS_TOPIC, key, eventType);
                    } else {
                        log.error("Auction 이벤트 발행 실패: topic={}, key={}, eventType={}, error={}",
                                KafkaTopicConfig.AUCTION_EVENTS_TOPIC, key, eventType, ex.getMessage(), ex);
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
            log.error("Auction 이벤트 동기 발행 실패: key={}, eventType={}", key, eventType, e);
            throw new RuntimeException("Auction Kafka 메시지 발행 실패", e);
        }
    }
}
