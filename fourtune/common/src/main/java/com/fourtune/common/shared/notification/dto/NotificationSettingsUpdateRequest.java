package com.fourtune.common.shared.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingsUpdateRequest(
        @NotNull(message = "입찰 알림 설정값은 필수입니다.")
        boolean isBidPushEnabled,

        @NotNull(message = "결제 알림 설정값은 필수입니다.")
        boolean isPaymentPushEnabled,

        @NotNull(message = "관심상품 알림 설정값은 필수입니다.")
        boolean isWatchListPushEnabled
) {
}
