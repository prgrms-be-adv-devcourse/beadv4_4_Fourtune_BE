package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.boundedContext.notification.domain.NotificationUser;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationRepository;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationUserRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationSupport {

    private final NotificationRepository notificationRepository;
    private final NotificationUserRepository notificationUserRepository;

    public List<Notification> findAllByUserIdOrderBySendAtDesc(Long userId){
        return notificationRepository.findAllByUserIdOrderBySendAtDesc(userId);
    }

    public Notification findById(Long notificationId){
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    public NotificationUser findByUserId(Long userId){
        return notificationUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public Notification save(Notification notification){
        return notificationRepository.save(notification);
    }

    public void delete(Notification notification){
        notificationRepository.delete(notification);
    }

}
