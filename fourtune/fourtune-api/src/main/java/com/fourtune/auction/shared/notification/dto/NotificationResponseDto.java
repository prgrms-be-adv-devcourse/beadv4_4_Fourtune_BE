package com.fourtune.auction.shared.notification.dto;

import com.fourtune.auction.boundedContext.notification.domain.Notification;
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
}
