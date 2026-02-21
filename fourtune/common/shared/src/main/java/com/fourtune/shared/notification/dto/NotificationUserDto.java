package com.fourtune.shared.notification.dto;

import java.time.LocalDateTime;

public record NotificationUserDto (
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String nickname
){}
