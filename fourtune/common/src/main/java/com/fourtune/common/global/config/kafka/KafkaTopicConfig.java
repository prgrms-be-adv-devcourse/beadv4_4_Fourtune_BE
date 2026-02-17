package com.fourtune.common.global.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTopicConfig {

    public static final String USER_EVENTS_TOPIC = "user-account-events";
    public static final String USER_EVENTS_DLQ_TOPIC = "user-events-dlq";

    public static final String AUCTION_EVENTS_TOPIC = "auction-events";
    public static final String AUCTION_EVENTS_DLQ_TOPIC = "auction-events-dlq";

    public static final String SEARCH_LOG_EVENTS_TOPIC = "search-log-events";

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(USER_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userEventsDlqTopic() {
        return TopicBuilder.name(USER_EVENTS_DLQ_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auctionEventsTopic() {
        return TopicBuilder.name(AUCTION_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auctionEventsDlqTopic() {
        return TopicBuilder.name(AUCTION_EVENTS_DLQ_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // TODO: 추후 검색 로그 유실 방지가 중요해지면 DLQ 토픽 추가 고려
    @Bean
    public NewTopic searchLogEventsTopic() {
        return TopicBuilder.name(SEARCH_LOG_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}