package com.fourtune.auction.boundedContext.notification.application.service;

import com.fourtune.auction.boundedContext.notification.constant.NotificationType;
import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.shared.notification.dto.NotificationResponse;
import com.fourtune.auction.shared.notification.dto.UnreadCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 조회 UseCase
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryUseCase {

    private final NotificationSupport notificationSupport;

    /**
     * 알림 목록 조회 (페이징)
     */
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationSupport.findByUserId(userId, pageable)
                .map(NotificationResponse::from);
    }

    /**
     * 읽지 않은 알림 목록 조회
     */
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationSupport.findUnreadByUserId(userId).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationSupport.countUnread(userId);
        return new UnreadCountResponse(count);
    }

    /**
     * 알림 상세 조회
     */
    public NotificationResponse getNotification(Long notificationId, Long userId) {
        Notification notification = notificationSupport.findByIdOrThrow(notificationId);
        notificationSupport.validateOwner(notification, userId);
        return NotificationResponse.from(notification);
    }

    /**
     * 특정 타입 알림 조회
     */
    public List<NotificationResponse> getNotificationsByType(Long userId, NotificationType type) {
        return notificationSupport.findByUserIdAndType(userId, type).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

}
