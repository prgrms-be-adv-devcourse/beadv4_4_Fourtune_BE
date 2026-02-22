package com.fourtune.auction.boundedContext.notification.mapper;

import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.boundedContext.notification.domain.NotificationSettings;
import com.fourtune.shared.notification.dto.NotificationResponseDto;
import com.fourtune.shared.notification.dto.NotificationSettingsResponse;
import org.springframework.stereotype.Component;

public class NotificationMapper {

    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .sendAt(notification.getSendAt())
                .build();
    }

    public static NotificationSettingsResponse from(NotificationSettings entity) {
        return new NotificationSettingsResponse(
                entity.isBidPushEnabled(),
                entity.isPaymentPushEnabled(),
                entity.isWatchListPushEnabled()
        );
    }

}
