package com.fourtune.common.shared.notification.kafka;

import com.fourtune.common.global.config.kafka.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Notification 이벤트 Kafka Producer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationKafkaProducer {

    private static final String HEADER_EVENT_TYPE = "X-Event-Type";

    private final KafkaTemplate<String, String> auctionKafkaTemplate;

    public void send(String key, String payload, String eventType) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopicConfig.NOTIFICATION_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(HEADER_EVENT_TYPE, eventType)
                .build();
        auctionKafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Notification 이벤트 발행 성공: key={}, eventType={}", key, eventType);
                    } else {
                        log.error("Notification 이벤트 발행 실패: key={}, eventType={}, error={}",
                                key, eventType, ex.getMessage(), ex);
                    }
                });
    }
}
