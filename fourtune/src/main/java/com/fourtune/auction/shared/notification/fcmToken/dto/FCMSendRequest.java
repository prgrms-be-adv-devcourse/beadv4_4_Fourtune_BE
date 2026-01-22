package com.fourtune.auction.shared.notification.fcmToken.dto;

import lombok.Builder;

@Builder
public record FCMSendRequest(
        String token,
        String title,
        String body
) {}
