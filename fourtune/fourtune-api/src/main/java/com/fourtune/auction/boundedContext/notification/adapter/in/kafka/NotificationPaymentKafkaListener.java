package com.fourtune.auction.boundedContext.notification.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.shared.kafka.payment.PaymentEventMapper;
import com.fourtune.shared.payment.event.PaymentFailedEvent;
import com.fourtune.shared.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Notification 도메인의 Payment 이벤트 Kafka Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationPaymentKafkaListener {

    private final NotificationFacade notificationFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.PAYMENT_EVENTS_TOPIC,
            groupId = "notification-payment-events-group",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null) {
            return;
        }

        try {
            PaymentEventMapper.EventType type = PaymentEventMapper.EventType.valueOf(eventType);

            switch (type) {
                case PAYMENT_SUCCEEDED -> handlePaymentSucceeded(payload);
                case PAYMENT_FAILED -> handlePaymentFailed(payload);
                default -> {
                    // 알림 도메인에서 처리하지 않는 이벤트는 무시
                }
            }
        } catch (IllegalArgumentException e) {
            log.warn("[Notification] 알 수 없는 결제 이벤트 타입: {}", eventType);
        } catch (Exception e) {
            log.error("[Notification] Payment 이벤트 처리 실패: eventType={}, error={}", eventType, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void handlePaymentSucceeded(String payload) throws Exception {
        PaymentSucceededEvent event = objectMapper.readValue(payload, PaymentSucceededEvent.class);
        Long userId = event.getOrder().getUserId();
        log.info("[Notification] 결제 성공 이벤트 수신 - userId={}", userId);

        if (event.getOrder().getItems() == null || event.getOrder().getItems().isEmpty()) {
            log.warn("[Notification] 결제 성공 이벤트 처리 실패: Order에 items가 없음");
            return;
        }

        Long auctionId = event.getOrder().getItems().get(0).getItemId();
        String auctionTitle = event.getOrder().getItems().get(0).getItemName() != null
                ? event.getOrder().getItems().get(0).getItemName() : "";

        notificationFacade.createNotification(userId, auctionId, NotificationType.PAYMENT_SUCCESS, auctionTitle);
    }

    private void handlePaymentFailed(String payload) throws Exception {
        PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);
        if (event.getOrder() == null) {
            log.warn("[Notification] 결제 실패 이벤트 처리 실패: Order가 null");
            return;
        }

        Long userId = event.getOrder().getUserId();
        log.info("[Notification] 결제 실패 이벤트 수신 - userId={}", userId);

        if (event.getOrder().getItems() == null || event.getOrder().getItems().isEmpty()) {
            log.warn("[Notification] 결제 실패 이벤트 처리 실패: Order에 items가 없음");
            return;
        }

        Long auctionId = event.getOrder().getItems().get(0).getItemId();
        String auctionTitle = event.getOrder().getItems().get(0).getItemName() != null
                ? event.getOrder().getItems().get(0).getItemName() : "";

        notificationFacade.createNotification(userId, auctionId, NotificationType.PAYMENT_FAILED, auctionTitle);
    }
}
