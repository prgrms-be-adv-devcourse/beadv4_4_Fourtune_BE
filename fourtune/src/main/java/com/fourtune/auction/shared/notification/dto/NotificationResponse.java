package com.fourtune.auction.shared.notification.dto;

import com.fourtune.auction.boundedContext.notification.constant.NotificationType;
import com.fourtune.auction.boundedContext.notification.domain.Notification;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
public record NotificationResponse(
        long id,
        NotificationType type,
        String typeDescription,
        String title,
        String content,
        boolean isRead,
        LocalDateTime readAt,
        Long relatedId,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getType().getDescription(),
                notification.getTitle(),
                notification.getContent(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getRelatedId(),
                notification.getCreatedAt()
        );
    }
}
