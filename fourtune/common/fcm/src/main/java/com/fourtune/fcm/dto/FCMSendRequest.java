package com.fourtune.fcm.dto;

import lombok.Builder;

@Builder
public record FCMSendRequest(
        String token,
        String title,
        String body
) {}
