package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeleteUseCase {

    private final NotificationSupport notificationSupport;

    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationSupport.findById(notificationId);

        if (!notification.isOwnedBy(userId)) {
            log.warn("권한 없는 알림 삭제 시도 - User: {}, Notification: {}", userId, notificationId);
            throw new BusinessException(ErrorCode.NOT_NOTIFICATION_OWNER);
        }

        notificationSupport.delete(notification);
        log.info("알림 삭제 완료 - ID: {}", notificationId);
    }

}
