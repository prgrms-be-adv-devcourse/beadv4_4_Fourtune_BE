package com.fourtune.common.shared.notification.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponseDto(
        Long id,
        String type,
        String title,
        String content,
        String relatedUrl,
        boolean isRead,
        LocalDateTime sendAt
) {
}
