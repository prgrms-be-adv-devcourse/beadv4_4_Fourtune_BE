package com.fourtune.auction.boundedContext.fcmToken.adapter.in;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.fcmToken.application.FcmService;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.api.infrastructure.kafka.notification.NotificationEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * FCM 푸시 알림용 Notification 이벤트 Kafka Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.notification-events.enabled", havingValue = "true", matchIfMissing = false)
public class FcmNotificationKafkaListener {

    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.NOTIFICATION_EVENTS_TOPIC,
            groupId = "fcm-notification-events-group",
            containerFactory = "notificationEventKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null || !eventType.equals(NotificationEventType.NOTIFICATION_CREATED.name())) {
            return;
        }

        try {
            JsonNode node = objectMapper.readTree(payload);
            Long receiverId = node.get("receiverId").asLong();
            String title = node.get("title").asText();
            String content = node.get("content").asText();
            String relatedUrl = node.get("relatedUrl").asText();

            fcmService.sendNotification(receiverId, title, content, relatedUrl);
            log.debug("[FCM] Notification 이벤트 처리 완료: receiverId={}", receiverId);

        } catch (Exception e) {
            log.error("[FCM] Notification 이벤트 처리 실패: error={}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
