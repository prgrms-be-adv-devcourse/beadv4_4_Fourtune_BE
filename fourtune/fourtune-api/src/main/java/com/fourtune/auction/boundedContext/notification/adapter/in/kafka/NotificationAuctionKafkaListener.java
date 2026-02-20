package com.fourtune.auction.boundedContext.notification.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.common.global.config.kafka.KafkaTopicConfig;
import com.fourtune.common.shared.auction.event.AuctionBuyNowEvent;
import com.fourtune.common.shared.auction.event.AuctionClosedEvent;
import com.fourtune.common.shared.auction.event.AuctionExtendedEvent;
import com.fourtune.common.shared.auction.event.BidCanceledEvent;
import com.fourtune.common.shared.auction.event.BidPlacedEvent;
import com.fourtune.common.shared.auction.kafka.AuctionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Notification 도메인의 Auction 이벤트 Kafka Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.auction-events.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationAuctionKafkaListener {

    private final NotificationFacade notificationFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.AUCTION_EVENTS_TOPIC,
            groupId = "notification-auction-events-group",
            containerFactory = "auctionEventKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null) {
            return;
        }

        try {
            AuctionEventType type = AuctionEventType.valueOf(eventType);

            switch (type) {
                case BID_PLACED -> handleBidPlaced(payload);
                case AUCTION_CLOSED -> handleAuctionClosed(payload);
                case AUCTION_BUY_NOW -> handleAuctionBuyNow(payload);
                case AUCTION_EXTENDED -> handleAuctionExtended(payload);
                case BID_CANCELED -> handleBidCanceled(payload);
                default -> {
                    // 알림 도메인에서 처리하지 않는 이벤트는 무시
                }
            }
        } catch (IllegalArgumentException e) {
            log.warn("[Notification] 알 수 없는 이벤트 타입: {}", eventType);
        } catch (Exception e) {
            log.error("[Notification] Auction 이벤트 처리 실패: eventType={}, error={}", eventType, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void handleBidPlaced(String payload) throws Exception {
        BidPlacedEvent event = objectMapper.readValue(payload, BidPlacedEvent.class);
        log.info("[Notification] 입찰 이벤트 수신 - sellerId={}", event.sellerId());

        notificationFacade.bidPlaceToSeller(
                event.sellerId(), event.bidderId(), event.auctionId(), NotificationType.BID_RECEIVED);

        if (event.previousBidderId() != null) {
            log.info("[Notification] 상위 입찰 알림 발송 - target={}", event.previousBidderId());
            notificationFacade.createNotification(
                    event.previousBidderId(), event.auctionId(), NotificationType.OUTBID);
        }
    }

    private void handleAuctionClosed(String payload) throws Exception {
        AuctionClosedEvent event = objectMapper.readValue(payload, AuctionClosedEvent.class);
        log.info("[Notification] 경매종료 이벤트 수신 - sellerId={}, winnerId={}", event.sellerId(), event.winnerId());

        if (event.winnerId() == null) {
            notificationFacade.createNotification(
                    event.sellerId(), event.auctionId(), NotificationType.AUCTION_FAILED);
        } else {
            notificationFacade.createNotification(
                    event.winnerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);
            notificationFacade.createNotification(
                    event.sellerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);
        }
    }

    private void handleAuctionBuyNow(String payload) throws Exception {
        AuctionBuyNowEvent event = objectMapper.readValue(payload, AuctionBuyNowEvent.class);
        log.info("[Notification] 즉시구매 이벤트 수신 - auctionId={}, buyerId={}, sellerId={}",
                event.auctionId(), event.buyerId(), event.sellerId());

        notificationFacade.createNotification(
                event.buyerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);
        notificationFacade.createNotification(
                event.sellerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);
    }

    private void handleAuctionExtended(String payload) throws Exception {
        AuctionExtendedEvent event = objectMapper.readValue(payload, AuctionExtendedEvent.class);
        log.info("[Notification] 경매 연장 이벤트 수신 - auctionId={}, newEndTime={}",
                event.auctionId(), event.newEndTime());

        // TODO: 경매 연장 시 관심상품 사용자들에게 알림 발송
        // notificationFacade.createGroupNotification(users, event.auctionId(), NotificationType.AUCTION_EXTENDED);
    }

    private void handleBidCanceled(String payload) throws Exception {
        BidCanceledEvent event = objectMapper.readValue(payload, BidCanceledEvent.class);
        log.info("[Notification] 입찰 취소 이벤트 수신 - auctionId={}, sellerId={}", event.auctionId(), event.sellerId());

        notificationFacade.createNotification(
                event.sellerId(), event.auctionId(), NotificationType.BID_RECEIVED);
    }
}
