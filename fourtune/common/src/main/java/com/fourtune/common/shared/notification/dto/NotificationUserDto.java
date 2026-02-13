package com.fourtune.common.shared.notification.dto;

import java.time.LocalDateTime;

public record NotificationUserDto (
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String nickname
){}
