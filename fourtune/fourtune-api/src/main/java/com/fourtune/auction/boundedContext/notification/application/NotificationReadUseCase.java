package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.boundedContext.notification.mapper.NotificationMapper;
import com.fourtune.common.global.error.ErrorCode;
import com.fourtune.common.global.error.exception.BusinessException;
import com.fourtune.common.shared.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationReadUseCase {

    private final NotificationSupport notificationSupport;

    public List<NotificationResponseDto> readNotifications(Long userId){
        return notificationSupport.findAllByUserIdOrderBySendAtDesc(userId).stream()
                .map(NotificationMapper::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void isReadIsTrue(Long userId, Long notificationId){
        Notification notification = notificationSupport.findById(notificationId);

        if(!notification.isOwnedBy(userId)){
            log.warn("권한 없는 알림 접근 - User: {}, Notification: {}", userId, notificationId);
            throw new BusinessException(ErrorCode.NOT_NOTIFICATION_OWNER);
        }

        notification.read();
    }

}
