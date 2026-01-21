package com.fourtune.auction.boundedContext.notification.application.service;

import com.fourtune.auction.boundedContext.notification.constant.NotificationType;
import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 알림 공통 조회/검증 기능
 */
@Component
@RequiredArgsConstructor
public class NotificationSupport {

    private final NotificationRepository notificationRepository;

    /**
     * ID로 알림 조회 (없으면 예외)
     */
    public Notification findByIdOrThrow(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    /**
     * 알림 저장
     */
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * 사용자의 알림 목록 조회 (페이징)
     */
    public Page<Notification> findByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     */
    public List<Notification> findUnreadByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 사용자의 특정 타입 알림 조회
     */
    public List<Notification> findByUserIdAndType(Long userId, NotificationType type) {
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
    }

    /**
     * 알림 권한 검증 (본인 알림인지 확인)
     */
    public void validateOwner(Notification notification, Long userId) {
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 사용자의 모든 알림 읽음 처리
     */
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    /**
     * 읽은 알림 삭제
     */
    public void deleteReadNotifications(Long userId) {
        notificationRepository.deleteByUserIdAndIsReadTrue(userId);
    }

}
