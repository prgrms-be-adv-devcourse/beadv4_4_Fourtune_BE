package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.common.shared.notification.dto.NotificationResponseDto;
import com.fourtune.common.shared.notification.dto.NotificationSettingsResponse;
import com.fourtune.common.shared.notification.dto.NotificationSettingsUpdateRequest;
import com.fourtune.common.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationFacade {

    private final NotificationCreateUseCase notificationCreateUseCase;
    private final NotificationReadUseCase notificationReadUseCase;
    private final NotificationDeleteUseCase notificationDeleteUseCase;
    private final NotificationSyncUserUseCase notificationSyncUserUseCase;
    private final NotificationSettingsService notificationSettingsService;

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

    public void bidPlaceToSeller(Long sellerId, Long bidderId, Long auctionId, NotificationType type) {
        notificationCreateUseCase.bidPlaceToSeller(sellerId, bidderId, auctionId, type);
    }

    public void createNotification(Long receiverId, Long auctionId, NotificationType type){
        notificationCreateUseCase.createNotificationWithUrl(receiverId, auctionId, type);
    }

    public void createGroupNotification(List<Long> users, Long auctionItemId, NotificationType type){
        notificationCreateUseCase.createGroupNotification(users, auctionItemId, type);
    }

    public void createSettlementNotification(Long receiverId, Long settlementId, NotificationType type){
        notificationCreateUseCase.createSettlementNotification(receiverId, settlementId, type);
    }

}
