package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.boundedContext.notification.domain.NotificationUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationCreateUseCase {

    private final NotificationSupport notificationSupport;

    @Transactional
    public void createNotification(Long receiverId, NotificationType type, String title, String content, String relatedUrl){
        NotificationUser user = notificationSupport.findByUserId(receiverId);

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .relatedUrl(relatedUrl)
                .build();

        notificationSupport.save(notification);
        log.info("알림 생성 완료 - Receiver: {}, Type: {}", receiverId, type);
    }

}
