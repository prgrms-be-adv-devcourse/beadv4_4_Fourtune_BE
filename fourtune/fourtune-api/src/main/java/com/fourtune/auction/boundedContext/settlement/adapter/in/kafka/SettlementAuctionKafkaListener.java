package com.fourtune.auction.boundedContext.settlement.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.settlement.application.service.SettlementFacade;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.shared.auction.event.OrderCompletedEvent;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import com.fourtune.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * auction-events 토픽 구독 — 주문 완료(ORDER_COMPLETED) 시 정산 후보 등록 (MSA 분리 시 Auction → Settlement 연동)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.auction-events.enabled", havingValue = "true", matchIfMissing = false)
public class SettlementAuctionKafkaListener {

    private final ObjectMapper objectMapper;
    private final SettlementFacade settlementFacade;

    @KafkaListener(
            topics = KafkaTopicConfig.AUCTION_EVENTS_TOPIC,
            groupId = "settlement-auction-events-group",
            containerFactory = "auctionEventKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null) {
            return;
        }

        if (!AuctionEventType.ORDER_COMPLETED.name().equals(eventType)) {
            log.trace("[Settlement] 무시하는 경매 이벤트: eventType={}", eventType);
            return;
        }

        log.info("[Settlement] 주문 완료 이벤트 수신 → 정산 후보 등록: eventType={}", eventType);

        try {
            OrderCompletedEvent event = objectMapper.readValue(payload, OrderCompletedEvent.class);
            OrderDto orderDto = OrderDto.from(event);
            settlementFacade.addSettlementCandidatedItem(orderDto);
            log.info("[Settlement] 정산 후보 등록 완료: orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("[Settlement] ORDER_COMPLETED 처리 실패: eventType={}, payload={}, error={}",
                    eventType, payload, e.getMessage(), e);
            throw new RuntimeException("Settlement ORDER_COMPLETED 처리 실패", e);
        }
    }
}
