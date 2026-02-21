package com.fourtune.shared.notification.dto;

public record NotificationSettingsResponse(
        boolean isBidPushEnabled,
        boolean isPaymentPushEnabled,
        boolean isWatchListPushEnabled
) {

}
