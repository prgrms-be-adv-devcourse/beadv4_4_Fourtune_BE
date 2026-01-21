package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.auction.shared.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationCreateUseCase notificationCreateUseCase;
    private final NotificationReadUseCase notificationReadUseCase;
    private final NotificationDeleteUseCase notificationDeleteUseCase;

    public void createNotification(Long receiverId, NotificationType type, String title, String content, String relatedUrl) {
        notificationCreateUseCase.createNotification(receiverId, type, title, content, relatedUrl);
    }

    public List<NotificationResponseDto> getMyNotifications(Long userId) {
        return notificationReadUseCase.readNotifications(userId);
    }

    public void markAsRead(Long userId, Long notificationId) {
        notificationReadUseCase.isReadIsTrue(userId, notificationId);
    }

    public void deleteNotification(Long userId, Long notificationId) {
        notificationDeleteUseCase.deleteNotification(userId, notificationId);
    }

}
