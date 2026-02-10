package com.fourtune.auction.global.config.kafka;

import com.fourtune.auction.shared.user.kafka.UserEventMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:fourtune-consumer-group}")
    private String consumerGroupId;

    // ObjectMapper는 JacksonConfig에서 정의한 단일 빈을 주입받음 (중복 빈 충돌 방지, JavaTimeModule로 UserEventMessage 등 날짜 필드 직렬화 보장)
    // 상세: docs/reference/OBJECTMAPPER_KAFKA_JACKSON_ANALYSIS.md
    // 혹시 모를 롤백용: 아래 주석 해제 시 이 클래스에서 ObjectMapper 빈 정의 (JacksonConfig와 중복 시 충돌)
    // @Bean
    // public ObjectMapper objectMapper() {
    //     return new ObjectMapper();
    // }

    // --- Producer 설정 ---

    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                new JacksonSerializer<>(objectMapper) // 이름 살짝 변경 (Jackson 2용)
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ObjectMapper objectMapper) {
        return new KafkaTemplate<>(producerFactory(objectMapper));
    }

    // --- Consumer 설정 ---

    @Bean
    public ConsumerFactory<String, UserEventMessage> userEventConsumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                new JacksonDeserializer<>(objectMapper, UserEventMessage.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEventMessage> userEventKafkaListenerContainerFactory(
            ConsumerFactory<String, UserEventMessage> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, UserEventMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));

        return factory;
    }

    // =========================================================================
    //  [Inner Classes] Jackson 2 (com.fasterxml) 기반 커스텀 구현
    // =========================================================================

    public static class JacksonSerializer<T> implements Serializer<T> {
        private final ObjectMapper objectMapper;

        public JacksonSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public byte[] serialize(String topic, T data) {
            if (data == null) return null;
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (JsonProcessingException e) {
                throw new SerializationException("Error serializing JSON message", e);
            }
        }
    }

    public static class JacksonDeserializer<T> implements Deserializer<T> {
        private final ObjectMapper objectMapper;
        private final Class<T> targetType;

        public JacksonDeserializer(ObjectMapper objectMapper, Class<T> targetType) {
            this.objectMapper = objectMapper;
            this.targetType = targetType;
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            if (data == null) return null;
            try {
                return objectMapper.readValue(data, targetType);
            } catch (IOException e) {
                throw new SerializationException("Error deserializing JSON message", e);
            }
        }
    }
}
