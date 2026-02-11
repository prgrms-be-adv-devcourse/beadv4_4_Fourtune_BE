package com.fourtune.auction.shared.user.kafka;

import com.fourtune.auction.global.config.kafka.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * User 이벤트 Kafka Producer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class UserKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * User 이벤트 발행
     * 파티션 키로 userId를 사용하여 같은 사용자의 이벤트가 순서대로 처리되도록 보장
     */
    public CompletableFuture<SendResult<String, Object>> publish(UserEventMessage message) {
        String partitionKey = String.valueOf(message.getUserId());

        log.info("Kafka 메시지 발행 시작: topic={}, key={}, eventType={}",
                KafkaTopicConfig.USER_EVENTS_TOPIC, partitionKey, message.getEventType());

        return kafkaTemplate.send(KafkaTopicConfig.USER_EVENTS_TOPIC, partitionKey, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka 메시지 발행 성공: topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Kafka 메시지 발행 실패: topic={}, key={}, error={}",
                                KafkaTopicConfig.USER_EVENTS_TOPIC, partitionKey, ex.getMessage(), ex);
                    }
                });
    }

    /**
     * 동기 발행 (테스트용)
     */
    public void publishSync(UserEventMessage message) {
        try {
            publish(message).get();
        } catch (Exception e) {
            log.error("Kafka 동기 발행 실패", e);
            throw new RuntimeException("Kafka 메시지 발행 실패", e);
        }
    }
}
