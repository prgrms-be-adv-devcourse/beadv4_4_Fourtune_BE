package com.fourtune.auction.boundedContext.fcmToken.adapter.in;

import com.fourtune.auction.boundedContext.fcmToken.application.FcmService;
import com.fourtune.common.shared.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FcmTokenEventListener {

    private final FcmService fcmService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event){
        fcmService.sendNotification(
                event.receiverId(),
                event.title(),
                event.content(),
                event.relatedUrl()
        );
    }

}
