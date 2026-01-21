package com.fourtune.auction.boundedContext.notification.application.service;

import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 삭제 UseCase
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeleteUseCase {

    private final NotificationSupport notificationSupport;
    private final NotificationRepository notificationRepository;

    /**
     * 단일 알림 삭제
     */
    @Transactional
    public void delete(Long notificationId, Long userId) {
        Notification notification = notificationSupport.findByIdOrThrow(notificationId);
        
        // 권한 검증
        notificationSupport.validateOwner(notification, userId);
        
        // 삭제
        notificationRepository.delete(notification);
        
        log.info("알림 삭제: notificationId={}, userId={}", notificationId, userId);
    }

    /**
     * 읽은 알림 전체 삭제
     */
    @Transactional
    public void deleteReadNotifications(Long userId) {
        notificationSupport.deleteReadNotifications(userId);
        
        log.info("읽은 알림 전체 삭제: userId={}", userId);
    }

}
