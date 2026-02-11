package com.fourtune.auction.boundedContext.payment.adapter.in.kafka;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.auction.global.config.kafka.KafkaTopicConfig;
import com.fourtune.auction.shared.user.kafka.UserEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
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

    @KafkaListener(
            topics = KafkaTopicConfig.USER_EVENTS_TOPIC,
            groupId = "payment-user-events-group",
            containerFactory = "userEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleUserEvent(@Payload UserEventMessage message, Acknowledgment ack) {
        try {
            log.info("[Payment] User 이벤트 수신: type={}, userId={}, messageId={}",
                    message.getEventType(), message.getUserId(), message.getMessageId());

            switch (message.getEventType()) {
                case USER_JOINED -> handleUserJoined(message);
                case USER_MODIFIED -> handleUserModified(message);
                case USER_DELETED -> handleUserDeleted(message);
            }

            ack.acknowledge();
            log.debug("[Payment] User 이벤트 처리 완료: messageId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("[Payment] User 이벤트 처리 실패: messageId={}, error={}",
                    message.getMessageId(), e.getMessage(), e);
            throw e;
        }
    }

    private void handleUserJoined(UserEventMessage message) {
        paymentFacade.syncUser(message.toUserResponse());
    }

    private void handleUserModified(UserEventMessage message) {
        paymentFacade.syncUser(message.toUserResponse());
    }

    private void handleUserDeleted(UserEventMessage message) {
        paymentFacade.deleteUser(message.toUserResponse());
    }
}
