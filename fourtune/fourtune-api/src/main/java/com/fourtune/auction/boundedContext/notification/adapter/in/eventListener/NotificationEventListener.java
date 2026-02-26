package com.fourtune.auction.boundedContext.notification.adapter.in.eventListener;

import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.application.NotificationSettingsService;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.shared.auction.event.AuctionBuyNowEvent;
import com.fourtune.shared.auction.event.AuctionClosedEvent;
import com.fourtune.shared.auction.event.AuctionExtendedEvent;
import com.fourtune.shared.auction.event.BidCanceledEvent;
import com.fourtune.shared.auction.event.BidPlacedEvent;
import com.fourtune.shared.payment.event.PaymentFailedEvent;
import com.fourtune.shared.payment.event.PaymentSucceededEvent;
import com.fourtune.shared.settlement.event.SettlementCompletedEvent;
import com.fourtune.shared.user.event.UserDeletedEvent;
import com.fourtune.shared.user.event.UserJoinedEvent;
import com.fourtune.shared.user.event.UserModifiedEvent;
import com.fourtune.shared.watchList.event.WatchListAuctionEndedEvent;
import com.fourtune.shared.watchList.event.WatchListAuctionStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationFacade notificationFacade;
    private final NotificationSettingsService notificationSettingsService;
    private final EventPublishingConfig eventPublishingConfig;

    // 유저 변경 이벤트 (Kafka 활성화 시 비활성화)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserJoinEvent(UserJoinedEvent event) {
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            log.debug("[Notification] User 이벤트는 Kafka로 처리됨 - Spring Event 무시");
            return;
        }
        log.info("현재 스레드: {}", Thread.currentThread().getName());
        notificationFacade.syncUser(event.getUser());
        notificationSettingsService.createNotificationSettings(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserModifiedEvent(UserModifiedEvent event) {
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            return;
        }
        notificationFacade.syncUser(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            return;
        }
        notificationFacade.syncUser(event.getUser());
    }

    // 정산 이벤트
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSettlementCompletedEvent(SettlementCompletedEvent event) {
        if (eventPublishingConfig.isKafkaEnabled()) {
            log.debug("[Notification] Settlement 이벤트는 Kafka로 처리됨 - Spring Event 무시");
            return;
        }
        Long payeeId = event.getSettlementDto().getPayeeId();
        Long settlementId = event.getSettlementDto().getId();
        String auctionTitle = event.getSettlementDto().getAuctionTitle() != null
                ? event.getSettlementDto().getAuctionTitle() : "";
        log.info("정산 완료 알림 발송: payeeId={}, settlementId={}", payeeId, settlementId);
        notificationFacade.createSettlementNotification(payeeId, settlementId, NotificationType.SETTLEMENT_SUCCESS, auctionTitle);
    }

    // 경매 이벤트 (Kafka 활성화 시 비활성화)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidPlacedEvent(BidPlacedEvent event) {
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            return;
        }
        log.info("입찰 이벤트 수신 - ReceiverId: {}", event.sellerId());

        notificationFacade.bidPlaceToSeller(event.sellerId(), event.bidderId(), event.auctionId(), NotificationType.BID_RECEIVED);

        if (event.previousBidderId() != null) {
            log.info("상위 입찰 알림 발송 - Target: {}", event.previousBidderId());
            notificationFacade.createNotification(event.previousBidderId(), event.auctionId(), NotificationType.OUTBID,
                    event.auctionTitle(), event.bidAmount());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionClosedEvent(AuctionClosedEvent event) {
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            return;
        }
        log.info("낙찰 이벤트 수신 - ReceiverId: {}", event.winnerId());
        log.info("경매종료 이벤트 수신 - ReceiverId: {}", event.sellerId());

        if (event.winnerId() == null) {
            notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.AUCTION_FAILED,
                    event.auctionTitle());
        } else {
            notificationFacade.createNotification(event.winnerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS,
                    event.auctionTitle(), event.finalPrice());
            notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS,
                    event.auctionTitle(), event.finalPrice());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionBuyNowEvent(AuctionBuyNowEvent event) {
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            return;
        }
        log.info("즉시구매 이벤트 수신 - auctionId={}, buyerId={}, sellerId={}",
                event.auctionId(), event.buyerId(), event.sellerId());

        notificationFacade.createNotification(event.buyerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS,
                event.auctionTitle(), event.buyNowPrice());
        notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS,
                event.auctionTitle(), event.buyNowPrice());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionExtendedEvent(AuctionExtendedEvent event) {
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            return;
        }
        log.info("경매 연장 이벤트 수신 - auctionId={}, newEndTime={}",
                event.auctionId(), event.newEndTime());

        // TODO: 경매 연장 시 관심상품 사용자들에게 알림 발송
        // notificationFacade.createGroupNotification(users, event.auctionId(), NotificationType.AUCTION_EXTENDED);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidCanceledEvent(BidCanceledEvent event) {
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            return;
        }
        log.info("입찰 취소 이벤트 수신 - auctionId={}, bidderId={}, sellerId={}",
                event.auctionId(), event.bidderId(), event.sellerId());

        notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.BID_RECEIVED);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSucceededEvent(PaymentSucceededEvent event) {
        if (eventPublishingConfig.isKafkaEnabled()) {
            log.debug("[Notification] Payment 이벤트는 Kafka로 처리됨 - Spring Event 무시");
            return;
        }
        log.info("결제 성공 이벤트 수신 - orderId={}, userId={}",
                event.getOrder().getAuctionOrderId(), event.getOrder().getUserId());

        Long userId = event.getOrder().getUserId();
        if (event.getOrder().getItems() != null && !event.getOrder().getItems().isEmpty()) {
            Long auctionId = event.getOrder().getItems().get(0).getItemId();
            String auctionTitle = event.getOrder().getItems().get(0).getItemName() != null
                    ? event.getOrder().getItems().get(0).getItemName() : "";
            notificationFacade.createNotification(userId, auctionId, NotificationType.PAYMENT_SUCCESS, auctionTitle);
        } else {
            log.warn("결제 성공 이벤트 처리 실패: Order에 items가 없음 - orderId={}", event.getOrder().getAuctionOrderId());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        if (eventPublishingConfig.isKafkaEnabled()) {
            log.debug("[Notification] Payment 이벤트는 Kafka로 처리됨 - Spring Event 무시");
            return;
        }
        log.info("결제 실패 이벤트 수신 - orderId={}, msg={}",
                event.getOrder() != null ? event.getOrder().getAuctionOrderId() : "null", event.getMsg());

        if (event.getOrder() != null) {
            Long userId = event.getOrder().getUserId();
            if (event.getOrder().getItems() != null && !event.getOrder().getItems().isEmpty()) {
                Long auctionId = event.getOrder().getItems().get(0).getItemId();
                String auctionTitle = event.getOrder().getItems().get(0).getItemName() != null
                        ? event.getOrder().getItems().get(0).getItemName() : "";
                notificationFacade.createNotification(userId, auctionId, NotificationType.PAYMENT_FAILED, auctionTitle);
            } else {
                log.warn("결제 실패 이벤트 처리 실패: Order에 items가 없음 - orderId={}",
                        event.getOrder().getAuctionOrderId());
            }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWatchListAuctionStartEvent(WatchListAuctionStartedEvent event) {
        if (eventPublishingConfig.isWatchlistEventsKafkaEnabled()) {
            return;
        }
        notificationFacade.createGroupNotification(event.getUsers(), event.getAuctionItemId(), NotificationType.WATCHLIST_START,
                event.getAuctionTitle());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWatchListAuctionEndEvent(WatchListAuctionEndedEvent event) {
        if (eventPublishingConfig.isWatchlistEventsKafkaEnabled()) {
            return;
        }
        notificationFacade.createGroupNotification(event.getUsers(), event.getAuctionItemId(), NotificationType.WATCHLIST_END,
                event.getAuctionTitle());
    }
}
