package com.fourtune.auction.boundedContext.notification.application.service;

import com.fourtune.auction.boundedContext.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 읽음 처리 UseCase
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationReadUseCase {

    private final NotificationSupport notificationSupport;

    /**
     * 단일 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationSupport.findByIdOrThrow(notificationId);
        
        // 권한 검증
        notificationSupport.validateOwner(notification, userId);
        
        // 읽음 처리
        notification.markAsRead();
        
        log.debug("알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);
    }

    /**
     * 사용자의 모든 알림 읽음 처리
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        int count = notificationSupport.markAllAsRead(userId);
        
        log.info("전체 알림 읽음 처리: userId={}, count={}", userId, count);
        
        return count;
    }

}
