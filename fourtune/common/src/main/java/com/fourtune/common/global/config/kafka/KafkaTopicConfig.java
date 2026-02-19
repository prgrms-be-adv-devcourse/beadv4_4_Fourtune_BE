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

    public static final String WATCHLIST_EVENTS_TOPIC = "watchlist-events";
    public static final String NOTIFICATION_EVENTS_TOPIC = "notification-events";

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

    @Bean
    public NewTopic watchlistEventsTopic() {
        return TopicBuilder.name(WATCHLIST_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(NOTIFICATION_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
