package com.fourtune.auction.boundedContext.notification.adapter.in.eventListener;

import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.application.NotificationSettingsService;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.auction.shared.user.event.UserDeletedEvent;
import com.fourtune.auction.shared.user.event.UserJoinedEvent;
import com.fourtune.auction.shared.user.event.UserModifiedEvent;
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
/*
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutBidEvent(OutBidEvent event) {
        log.info("📢 상위 입찰 이벤트 수신 - ReceiverId: {}", event.receiverId());

        NotificationType type = NotificationType.OUTBID;
        String title = type.getTitle();
        String content = type.makeContent(event.itemTitle(), event.newPrice());

        String relatedUrl = "/auctions/" + event.auctionItemId();

        notificationFacade.createNotification(event.receiverId(), type, title, content, relatedUrl);
    }
*/
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserJoinEvent(UserJoinedEvent event){
        notificationFacade.syncUser(event.getUser());
        notificationSettingsService.createNotificationSettings(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserModifiedEvent(UserModifiedEvent event){
        notificationFacade.syncUser(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeletedEvent(UserDeletedEvent event){
        notificationFacade.syncUser(event.getUser());
    }

}
