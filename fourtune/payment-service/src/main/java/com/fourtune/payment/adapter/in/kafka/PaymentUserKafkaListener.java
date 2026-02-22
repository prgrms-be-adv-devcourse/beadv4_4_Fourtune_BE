package com.fourtune.payment.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.payment.application.service.PaymentFacade;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment 도메인의 User 이벤트 Kafka Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.user-events.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentUserKafkaListener {

    private final PaymentFacade paymentFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.USER_EVENTS_TOPIC,
            groupId = "payment-user-events-group",
            containerFactory = "userEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleUserEvent(String payload,
                                @Header("X-Event-Type") String eventType,
                                Acknowledgment ack) {
        try {
            log.info("[Payment] User 이벤트 수신: type={}", eventType);

            UserResponse user = objectMapper.readValue(payload, UserResponse.class);
            UserEventType type = UserEventType.valueOf(eventType);

            switch (type) {
                case USER_JOINED, USER_MODIFIED -> paymentFacade.syncUser(user);
                case USER_DELETED -> paymentFacade.deleteUser(user);
            }

            ack.acknowledge();
            log.debug("[Payment] User 이벤트 처리 완료: type={}, userId={}", eventType, user.id());

        } catch (Exception e) {
            log.error("[Payment] User 이벤트 처리 실패: type={}, error={}", eventType, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
