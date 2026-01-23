package com.fourtune.auction.boundedContext.notification.adapter.in.eventListener;

import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.application.NotificationSettingsService;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.auction.shared.auction.event.AuctionBuyNowEvent;
import com.fourtune.auction.shared.auction.event.AuctionClosedEvent;
import com.fourtune.auction.shared.auction.event.AuctionExtendedEvent;
import com.fourtune.auction.shared.auction.event.BidCanceledEvent;
import com.fourtune.auction.shared.auction.event.BidPlacedEvent;
import com.fourtune.auction.shared.payment.event.PaymentFailedEvent;
import com.fourtune.auction.shared.payment.event.PaymentSucceededEvent;
import com.fourtune.auction.shared.settlement.event.SettlementCompletedEvent;
import com.fourtune.auction.shared.user.event.UserDeletedEvent;
import com.fourtune.auction.shared.user.event.UserJoinedEvent;
import com.fourtune.auction.shared.user.event.UserModifiedEvent;
import com.fourtune.auction.shared.watchList.event.WatchListAuctionEndedEvent;
import com.fourtune.auction.shared.watchList.event.WatchListAuctionStartedEvent;
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

    // 유저 변경 이벤트
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserJoinEvent(UserJoinedEvent event) {
        log.info("현재 스레드: {}", Thread.currentThread().getName());
        notificationFacade.syncUser(event.getUser());
        notificationSettingsService.createNotificationSettings(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserModifiedEvent(UserModifiedEvent event) {
        notificationFacade.syncUser(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        notificationFacade.syncUser(event.getUser());
    }

    // 정산 이벤트
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSettlementCompletedEvent(SettlementCompletedEvent event) {
        Long payeeId = event.getSettlementDto().getPayeeId();
        Long settlementId = event.getSettlementDto().getId();
        log.info("정산 완료 알림 발송: payeeId={}, settlementId={}", payeeId, settlementId);
        notificationFacade.createSettlementNotification(payeeId, settlementId, NotificationType.SETTLEMENT_SUCCESS);
    }

    // 경매 이벤트
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidPlacedEvent(BidPlacedEvent event) {
        log.info("📢 입찰 이벤트 수신 - ReceiverId: {}", event.sellerId());

        notificationFacade.bidPlaceToSeller(event.sellerId(), event.bidderId(), event.auctionId(), NotificationType.BID_RECEIVED);

        if (event.previousBidderId() != null) {
            log.info("📢 상위 입찰 알림 발송 - Target: {}", event.previousBidderId());
            notificationFacade.createNotification(event.previousBidderId(), event.auctionId(), NotificationType.OUTBID);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionClosedEvent(AuctionClosedEvent event) {
        log.info("낙찰 이벤트 수신 - ReceiverId: {}", event.winnerId());
        log.info("경매종료 이벤트 수신 - ReceiverId: {}", event.sellerId());

        if (event.winnerId() == null) {
            notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.AUCTION_FAILED);
        } else {
            notificationFacade.createNotification(event.winnerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);
            notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionBuyNowEvent(AuctionBuyNowEvent event) {
        log.info("즉시구매 이벤트 수신 - auctionId={}, buyerId={}, sellerId={}",
                event.auctionId(), event.buyerId(), event.sellerId());

        // 구매자에게 즉시구매 완료 알림
        notificationFacade.createNotification(event.buyerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);

        // 판매자에게 즉시구매 완료 알림
        notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionExtendedEvent(AuctionExtendedEvent event) {
        log.info("경매 연장 이벤트 수신 - auctionId={}, newEndTime={}",
                event.auctionId(), event.newEndTime());

        // TODO: 경매 연장 시 관심상품 사용자들에게 알림 발송
        // 현재는 경매 연장 알림 타입이 없으므로, 향후 WatchList 조회 후 그룹 알림 발송 필요
        // notificationFacade.createGroupNotification(users, event.auctionId(), NotificationType.AUCTION_EXTENDED);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidCanceledEvent(BidCanceledEvent event) {
        log.info("입찰 취소 이벤트 수신 - auctionId={}, bidderId={}, sellerId={}",
                event.auctionId(), event.bidderId(), event.sellerId());

        // 판매자에게 입찰 취소 알림
        notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.BID_RECEIVED);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSucceededEvent(PaymentSucceededEvent event) {
        log.info("결제 성공 이벤트 수신 - orderId={}, userId={}",
                event.getOrder().getOrderId(), event.getOrder().getUserId());

        // 결제 성공 알림 (OrderDto의 items에서 첫 번째 item의 itemId를 auctionId로 사용)
        Long userId = event.getOrder().getUserId();
        if (event.getOrder().getItems() != null && !event.getOrder().getItems().isEmpty()) {
            Long auctionId = event.getOrder().getItems().get(0).getItemId();
            notificationFacade.createNotification(userId, auctionId, NotificationType.PAYMENT_SUCCESS);
        } else {
            log.warn("결제 성공 이벤트 처리 실패: Order에 items가 없음 - orderId={}", event.getOrder().getOrderId());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신 - orderId={}, msg={}",
                event.getOrder() != null ? event.getOrder().getOrderId() : "null", event.getMsg());

        // 결제 실패 알림
        if (event.getOrder() != null) {
            Long userId = event.getOrder().getUserId();
            if (event.getOrder().getItems() != null && !event.getOrder().getItems().isEmpty()) {
                Long auctionId = event.getOrder().getItems().get(0).getItemId();
                notificationFacade.createNotification(userId, auctionId, NotificationType.PAYMENT_FAILED);
            } else {
                log.warn("결제 실패 이벤트 처리 실패: Order에 items가 없음 - orderId={}",
                        event.getOrder().getOrderId());
            }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWatchListAuctionStartEvent(WatchListAuctionStartedEvent event) {
        notificationFacade.createGroupNotification(event.getUsers(), event.getAuctionItemId(), NotificationType.WATCHLIST_START);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWatchListAuctionEndEvent(WatchListAuctionEndedEvent event) {
        notificationFacade.createGroupNotification(event.getUsers(), event.getAuctionItemId(), NotificationType.WATCHLIST_END);
    }
}
