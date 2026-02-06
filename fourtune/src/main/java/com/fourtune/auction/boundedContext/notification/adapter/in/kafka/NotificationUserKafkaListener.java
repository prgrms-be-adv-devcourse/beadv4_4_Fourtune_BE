package com.fourtune.auction.boundedContext.notification.adapter.in.kafka;

import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.application.NotificationSettingsService;
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
 * Notification 도메인의 User 이벤트 Kafka Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.user-events.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationUserKafkaListener {

    private final NotificationFacade notificationFacade;
    private final NotificationSettingsService notificationSettingsService;

    @KafkaListener(
            topics = KafkaTopicConfig.USER_EVENTS_TOPIC,
            groupId = "notification-user-events-group",
            containerFactory = "userEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleUserEvent(@Payload UserEventMessage message, Acknowledgment ack) {
        try {
            log.info("[Notification] User 이벤트 수신: type={}, userId={}, messageId={}",
                    message.getEventType(), message.getUserId(), message.getMessageId());

            switch (message.getEventType()) {
                case USER_JOINED -> handleUserJoined(message);
                case USER_MODIFIED -> handleUserModified(message);
                case USER_DELETED -> handleUserDeleted(message);
            }

            ack.acknowledge();
            log.debug("[Notification] User 이벤트 처리 완료: messageId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("[Notification] User 이벤트 처리 실패: messageId={}, error={}",
                    message.getMessageId(), e.getMessage(), e);
            throw e;
        }
    }

    private void handleUserJoined(UserEventMessage message) {
        notificationFacade.syncUser(message.toUserResponse());
        notificationSettingsService.createNotificationSettings(message.toUserResponse());
    }

    private void handleUserModified(UserEventMessage message) {
        notificationFacade.syncUser(message.toUserResponse());
    }

    private void handleUserDeleted(UserEventMessage message) {
        notificationFacade.syncUser(message.toUserResponse());
    }
}
