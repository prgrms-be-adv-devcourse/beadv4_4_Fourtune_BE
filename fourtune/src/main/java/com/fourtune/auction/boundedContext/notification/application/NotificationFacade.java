package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.auction.shared.notification.dto.NotificationResponseDto;
import com.fourtune.auction.shared.notification.dto.NotificationSettingsResponse;
import com.fourtune.auction.shared.notification.dto.NotificationSettingsUpdateRequest;
import com.fourtune.auction.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationCreateUseCase notificationCreateUseCase;
    private final NotificationReadUseCase notificationReadUseCase;
    private final NotificationDeleteUseCase notificationDeleteUseCase;
    private final NotificationSyncUserUseCase notificationSyncUserUseCase;
    private final NotificationSettingsService notificationSettingsService;

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

    public void syncUser(UserResponse userResponse){
        notificationSyncUserUseCase.syncUser(userResponse);
    }

    public void updateSettings(Long userId, NotificationSettingsUpdateRequest request){
        notificationSettingsService.updateSettings(userId, request);
    }

    public NotificationSettingsResponse getSettings(Long userId){
        return notificationSettingsService.getSettings(userId);
    }

}
