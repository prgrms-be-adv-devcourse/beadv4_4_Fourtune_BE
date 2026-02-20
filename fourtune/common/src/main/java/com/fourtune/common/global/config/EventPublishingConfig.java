package com.fourtune.common.global.config;

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

    @Value("${feature.kafka.auction-events.enabled:false}")
    private boolean auctionEventsKafkaEnabled;

    @Value("${feature.kafka.watchlist-events.enabled:false}")
    private boolean watchlistEventsKafkaEnabled;

    @Value("${feature.kafka.notification-events.enabled:false}")
    private boolean notificationEventsKafkaEnabled;

    /**
     * User 이벤트에 대해 Kafka를 사용할지 여부
     */
    public boolean isUserEventsKafkaEnabled() {
        return kafkaEnabled && userEventsKafkaEnabled;
    }

    /**
     * 경매 이벤트에 대해 Kafka를 사용할지 여부
     */
    public boolean isAuctionEventsKafkaEnabled() {
        return kafkaEnabled && auctionEventsKafkaEnabled;
    }

    /**
     * 관심상품 이벤트에 대해 Kafka를 사용할지 여부
     */
    public boolean isWatchlistEventsKafkaEnabled() {
        return kafkaEnabled && watchlistEventsKafkaEnabled;
    }

    /**
     * 알림 이벤트에 대해 Kafka를 사용할지 여부
     */
    public boolean isNotificationEventsKafkaEnabled() {
        return kafkaEnabled && notificationEventsKafkaEnabled;
    }
}
