package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.NotificationSettings;
import com.fourtune.auction.boundedContext.notification.domain.NotificationUser;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationSettingsRepository;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationUserRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.notification.dto.NotificationSettingsResponse;
import com.fourtune.auction.shared.notification.dto.NotificationSettingsUpdateRequest;
import com.fourtune.auction.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingsService {

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final NotificationUserRepository notificationUserRepository;

    public void createNotificationSettings(UserResponse userResponse){
        NotificationUser user = notificationUserRepository.getReferenceById(userResponse.id());

        NotificationSettings notificationSettings = NotificationSettings.builder()
                .user(user)
                .build();

        notificationSettingsRepository.save(notificationSettings);
    }

    public void updateSettings(Long userId, NotificationSettingsUpdateRequest request) {
        NotificationSettings settings = notificationSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTING_NOT_FOUND));

        settings.update(
                request.isBidPushEnabled(),
                request.isPaymentPushEnabled(),
                request.isWatchListPushEnabled()
        );
    }

    public NotificationSettingsResponse getSettings(Long userId) {
        NotificationSettings settings = notificationSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTING_NOT_FOUND));

        return NotificationSettingsResponse.from(settings);
    }

}
