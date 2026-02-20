package com.fourtune.kafka.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.kafka.KafkaTopicConfig;
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
 * 검색 이벤트 Kafka Producer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class SearchKafkaProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> auctionKafkaTemplate;

    /**
     * 검색 로그 이벤트 발행
     * @param payload 이벤트 객체 (JSON 직렬화 대상)
     */
    public CompletableFuture<?> send(Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            // 검색 로그는 순서가 중요하지 않으므로 key는 null (라운드 로빈)
            // 단일 이벤트 타입만 취급하므로 별도의 헤더(X-Event-Type)는 사용하지 않음
            Message<String> message = MessageBuilder
                    .withPayload(jsonPayload)
                    .setHeader(KafkaHeaders.TOPIC, KafkaTopicConfig.SEARCH_LOG_EVENTS_TOPIC)
                    .build();

            return auctionKafkaTemplate.send(message)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.debug("Search 이벤트 발행 성공: topic={}", KafkaTopicConfig.SEARCH_LOG_EVENTS_TOPIC);
                        } else {
                            log.error("Search 이벤트 발행 실패: topic={}, error={}",
                                    KafkaTopicConfig.SEARCH_LOG_EVENTS_TOPIC, ex.getMessage(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Search 이벤트 직렬화 실패: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
