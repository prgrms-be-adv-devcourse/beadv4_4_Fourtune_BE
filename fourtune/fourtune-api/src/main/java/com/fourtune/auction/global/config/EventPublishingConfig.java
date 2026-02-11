package com.fourtune.auction.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 이벤트 발행 설정
 * Feature Flag를 통해 Spring Events와 Kafka 전환 제어
 */
@Getter
@Configuration
public class EventPublishingConfig {

    @Value("${feature.kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Value("${feature.kafka.user-events.enabled:false}")
    private boolean userEventsKafkaEnabled;

    /**
     * User 이벤트에 대해 Kafka를 사용할지 여부
     */
    public boolean isUserEventsKafkaEnabled() {
        return kafkaEnabled && userEventsKafkaEnabled;
    }

    /**
     * Spring Events를 사용할지 여부 (Kafka가 비활성화된 경우)
     */
    public boolean isSpringEventsEnabled() {
        return !isUserEventsKafkaEnabled();
    }
}
