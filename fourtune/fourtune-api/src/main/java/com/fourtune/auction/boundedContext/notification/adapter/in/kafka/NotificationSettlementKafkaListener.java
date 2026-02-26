package com.fourtune.auction.boundedContext.notification.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.shared.kafka.settlement.SettlementEventMapper;
import com.fourtune.shared.settlement.event.SettlementCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Notification 도메인의 Settlement 이벤트 Kafka Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationSettlementKafkaListener {

    private final NotificationFacade notificationFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.SETTLEMENT_EVENTS_TOPIC,
            groupId = "notification-settlement-events-group",
            containerFactory = "settlementEventKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null) {
            return;
        }

        try {
            SettlementEventMapper.EventType type = SettlementEventMapper.EventType.valueOf(eventType);

            switch (type) {
                case SETTLEMENT_COMPLETED -> handleSettlementCompleted(payload);
                default -> {
                    // 알림 도메인에서 처리하지 않는 이벤트는 무시
                }
            }
        } catch (IllegalArgumentException e) {
            log.warn("[Notification] 알 수 없는 정산 이벤트 타입: {}", eventType);
        } catch (Exception e) {
            log.error("[Notification] Settlement 이벤트 처리 실패: eventType={}, error={}", eventType, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void handleSettlementCompleted(String payload) throws Exception {
        SettlementCompletedEvent event = objectMapper.readValue(payload, SettlementCompletedEvent.class);
        Long payeeId = event.getSettlementDto().getPayeeId();
        Long settlementId = event.getSettlementDto().getId();
        String auctionTitle = event.getSettlementDto().getAuctionTitle() != null
                ? event.getSettlementDto().getAuctionTitle() : "";

        log.info("[Notification] 정산 완료 이벤트 수신 - payeeId={}, settlementId={}", payeeId, settlementId);
        notificationFacade.createSettlementNotification(payeeId, settlementId, NotificationType.SETTLEMENT_SUCCESS, auctionTitle);
    }
}
