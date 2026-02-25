package com.fourtune.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@EnableKafka
@Configuration
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:fourtune-consumer-group}")
    private String consumerGroupId;

    // --- Producer 설정 (String, String) ---

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new StringSerializer());
    }

    @Bean
    public KafkaTemplate<String, String> auctionKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 재시도 소진 시 실패 메시지를 원본 토픽명 + "-dlq" 토픽으로 전달 (메시지 유실 방지)
     */
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, String> kafkaTemplate) {
        BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> resolver =
                (record, ex) -> new TopicPartition(record.topic() + "-dlq", record.partition());
        return new DeadLetterPublishingRecoverer(kafkaTemplate, resolver);
    }

    @Bean
    public DefaultErrorHandler kafkaCommonErrorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        return new DefaultErrorHandler(deadLetterPublishingRecoverer, new FixedBackOff(1000L, 3));
    }

    // --- User Event Consumer 설정 (String 기반) ---

    @Bean
    public ConsumerFactory<String, String> userEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> userEventKafkaListenerContainerFactory(
            ConsumerFactory<String, String> userEventConsumerFactory,
            DefaultErrorHandler kafkaCommonErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userEventConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(kafkaCommonErrorHandler);
        return factory;
    }

    // --- Auction Event Consumer 설정 (String 기반) ---

    @Bean
    public ConsumerFactory<String, String> auctionEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> auctionEventKafkaListenerContainerFactory(
            ConsumerFactory<String, String> auctionEventConsumerFactory,
            DefaultErrorHandler kafkaCommonErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(auctionEventConsumerFactory);
        factory.setCommonErrorHandler(kafkaCommonErrorHandler);
        return factory;
    }

    // --- WatchList Event Consumer 설정 (String 기반) ---

    @Bean
    public ConsumerFactory<String, String> watchlistEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> watchlistEventKafkaListenerContainerFactory(
            ConsumerFactory<String, String> watchlistEventConsumerFactory,
            DefaultErrorHandler kafkaCommonErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(watchlistEventConsumerFactory);
        factory.setCommonErrorHandler(kafkaCommonErrorHandler);
        return factory;
    }

    // --- Payment Event Consumer 설정 (String 기반) ---

    @Bean
    public ConsumerFactory<String, String> paymentEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> paymentEventKafkaListenerContainerFactory(
            ConsumerFactory<String, String> paymentEventConsumerFactory,
            DefaultErrorHandler kafkaCommonErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentEventConsumerFactory);
        factory.setCommonErrorHandler(kafkaCommonErrorHandler);
        return factory;
    }

    // --- Settlement Event Consumer 설정 (String 기반) ---

    @Bean
    public ConsumerFactory<String, String> settlementEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> settlementEventKafkaListenerContainerFactory(
            ConsumerFactory<String, String> settlementEventConsumerFactory,
            DefaultErrorHandler kafkaCommonErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(settlementEventConsumerFactory);
        factory.setCommonErrorHandler(kafkaCommonErrorHandler);
        return factory;
    }

    // --- Search Log Event Consumer 설정 (유실 허용, DLQ 없음) ---

    @Bean
    public ConsumerFactory<String, String> searchLogEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // 추천은 신규 이벤트만 필요
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new StringDeserializer());
    }

    /**
     * 검색 로그 전용 에러 핸들러 — DLQ 없이 재시도 1회 후 skip
     * search-log-events-dlq 토픽이 존재하지 않으므로 공통 DLQ 핸들러 사용 불가
     */
    @Bean
    public DefaultErrorHandler searchLogErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(500L, 1));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> searchLogEventKafkaListenerContainerFactory(
            ConsumerFactory<String, String> searchLogEventConsumerFactory,
            DefaultErrorHandler searchLogErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(searchLogEventConsumerFactory);
        factory.setCommonErrorHandler(searchLogErrorHandler);
        return factory;
    }

    // --- Notification Event Consumer 설정 (String 기반) ---

    @Bean
    public ConsumerFactory<String, String> notificationEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> notificationEventKafkaListenerContainerFactory(
            ConsumerFactory<String, String> notificationEventConsumerFactory,
            DefaultErrorHandler kafkaCommonErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationEventConsumerFactory);
        factory.setCommonErrorHandler(kafkaCommonErrorHandler);
        return factory;
    }
}
