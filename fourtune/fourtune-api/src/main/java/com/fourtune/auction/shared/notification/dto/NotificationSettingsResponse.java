package com.fourtune.auction.shared.notification.dto;

import com.fourtune.auction.boundedContext.notification.domain.NotificationSettings;

public record NotificationSettingsResponse(
        boolean isBidPushEnabled,
        boolean isPaymentPushEnabled,
        boolean isWatchListPushEnabled
) {

    public static NotificationSettingsResponse from(NotificationSettings entity) {
        return new NotificationSettingsResponse(
                entity.isBidPushEnabled(),
                entity.isPaymentPushEnabled(),
                entity.isWatchListPushEnabled()
        );
    }
}
