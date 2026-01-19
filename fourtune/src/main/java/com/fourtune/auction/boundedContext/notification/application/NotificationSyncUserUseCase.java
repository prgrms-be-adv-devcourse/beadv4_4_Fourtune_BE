package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.NotificationUser;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSyncUserUseCase {

    private final NotificationSupport notificationSupport;
    private final EventPublisher eventPublisher;

    public void syncUser(UserResponse userResponse){
        log.info("유저 동기화 시작 - UserId: {}", userResponse.id());

        notificationSupport.findOptionalByUserId(userResponse.id())
                .ifPresentOrElse(
                        existingUser -> existingUser.syncProfile(userResponse.nickname(), userResponse.email()),
                        () -> {
                            NotificationUser newUser = NotificationUser.builder()
                                    .id(userResponse.id())
                                    .createdAt(userResponse.createdAt())
                                    .updatedAt(userResponse.updatedAt())
                                    .email(userResponse.email())
                                    .nickname(userResponse.nickname())
                                    .build();

                            notificationSupport.saveNotificationUser(newUser);
                        }
                );
    }

}
