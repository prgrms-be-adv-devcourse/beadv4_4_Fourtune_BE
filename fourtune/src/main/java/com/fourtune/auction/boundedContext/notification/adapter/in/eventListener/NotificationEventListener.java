//package com.fourtune.auction.boundedContext.notification.adapter.in.eventListener;
//
//import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
//import com.fourtune.auction.boundedContext.notification.application.NotificationSettingsService;
//import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
//import com.fourtune.auction.shared.auction.event.AuctionClosedEvent;
//import com.fourtune.auction.shared.settlement.event.SettlementCompletedEvent;
//import com.fourtune.auction.shared.user.event.UserDeletedEvent;
//import com.fourtune.auction.shared.user.event.UserJoinedEvent;
//import com.fourtune.auction.shared.user.event.UserModifiedEvent;
//import com.fourtune.auction.shared.user.event.UserSignedUpEvent;
//import com.fourtune.auction.shared.watchList.event.WatchListAuctionEndedEvent;
//import com.fourtune.auction.shared.watchList.event.WatchListAuctionStartedEvent;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class NotificationEventListener {
//
//    private final NotificationFacade notificationFacade;
//    private final NotificationSettingsService notificationSettingsService;
//
//    //유저 변경 이벤트
//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleUserJoinEvent(UserJoinedEvent event){
//        notificationFacade.syncUser(event.getUser());
//        notificationSettingsService.createNotificationSettings(event.getUser());
//    }
//
//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleUserModifiedEvent(UserModifiedEvent event){
//        notificationFacade.syncUser(event.getUser());
//    }
//
//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleUserDeletedEvent(UserDeletedEvent event){
//        notificationFacade.syncUser(event.getUser());
//    }
//
//    //정산 이벤트
//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleSettlementCompletedEvent(SettlementCompletedEvent event){
//        notificationFacade.createNotification(event.getPayeeId(), NotificationType.SETTLEMENT_SUCCESS);
//    }
//
//    //경매 이벤트
//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleBidPlaceEvent(BidPlaceEvent event) {
//        log.info("📢 입찰 이벤트 수신 - ReceiverId: {}", event.sellerId());
//
//        notificationFacade.bidPlaceToSeller(event.sellerId(), event.bidderId(), event.auctionId, NotificationType.BID_RECEIVED);
//
//        if (event.previousBidderId() != null) {
//            log.info("📢 상위 입찰 알림 발송 - Target: {}", event.previousBidderId());
//
//            notificationFacade.createNotification(event.previousBidderId(), event.auctionId(), NotificationType.OUTBID);
//        }
//    }
//
//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleAuctionClosedEvent(AuctionClosedEvent event){
//        log.info("낙찰 이벤트 수신 - ReceiverId: {}", event.winnerId());
//        log.info("경매종료 이벤트 수신 - ReceiverId: {}", event.sellerId());
//
//        if(event.winnerId() == null) notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.AUCTION_FAILED);
//        else{
//            notificationFacade.createNotification(event.winnerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);
//            notificationFacade.createNotification(event.sellerId(), event.auctionId(), NotificationType.AUCTION_SUCCESS);
//        }
//    }
//
//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleWatchListAuctionStartEvent(WatchListAuctionStartedEvent event){
//        notificationFacade.createGroupNotification(event.getUsers(), event.getAuctionItemId(), NotificationType.WATCHLIST_START);
//    }
//
//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleWatchListAuctionEndEvent(WatchListAuctionEndedEvent event){
//        notificationFacade.createGroupNotification(event.getUsers(), event.getAuctionItemId(), NotificationType.WATCHLIST_END);
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleUserSignup(UserSignedUpEvent event) {
//        log.info("📢 회원가입 이벤트 수신 - 알림 설정 생성 시작 (UserId: {})", event.userResponse().id());
//
//        notificationSettingsService.createNotificationSettings(event.userResponse());
//    }
//
//}
