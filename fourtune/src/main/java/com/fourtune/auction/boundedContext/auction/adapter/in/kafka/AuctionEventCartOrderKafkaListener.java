package com.fourtune.auction.boundedContext.auction.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.auction.application.service.CartSupport;
import com.fourtune.auction.global.config.kafka.KafkaTopicConfig;
import com.fourtune.auction.shared.auction.event.AuctionClosedEvent;
import com.fourtune.auction.shared.auction.kafka.AuctionEventMapper;
import com.fourtune.auction.shared.auction.kafka.AuctionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 경매 이벤트 → 장바구니 만료 (Consumer Group: auction-cart-order)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.auction-events.enabled", havingValue = "true", matchIfMissing = false)
public class AuctionEventCartOrderKafkaListener {

    private final CartSupport cartSupport;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.AUCTION_EVENTS_TOPIC,
            groupId = "auction-cart-order",
            containerFactory = "auctionEventKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null || !eventType.equals(AuctionEventType.AUCTION_CLOSED.name())) {
            return;
        }
        try {
            AuctionClosedEvent event = (AuctionClosedEvent) objectMapper.readValue(
                    payload, AuctionEventMapper.getClass(eventType));
            cartSupport.expireCartItemsByAuctionId(event.auctionId());
            log.debug("[auction-cart-order] Cart items expired: auctionId={}", event.auctionId());
        } catch (Exception e) {
            log.error("[auction-cart-order] Failed: eventType={}, error={}", eventType, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
