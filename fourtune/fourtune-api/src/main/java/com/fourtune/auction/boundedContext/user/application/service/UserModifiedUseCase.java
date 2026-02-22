package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.shared.user.dto.UserUpdateRequest;
import com.fourtune.shared.user.event.UserModifiedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserModifiedUseCase {

    private static final String AGGREGATE_TYPE = "User";

    private final UserSupport userSupport;
    private final EventPublisher eventPublisher;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    @Transactional
    public void penalty(Long userId) {
        User user = userSupport.findByIdOrThrow(userId);
        user.imposePenalty();

        if (user.getPenaltyScore() <= -30) {
            user.bannedUser();
            publishUserModifiedEvent(user);
        }
    }

    @Transactional
    public void update(Long userId, UserUpdateRequest request){
        User user = userSupport.findByIdOrThrow(userId);

        isActiveUser(user);
        validateUpdate(user, request);

        user.updateProfile(request.nickname(), request.phoneNumber());
        publishUserModifiedEvent(user);
    }

    private void publishUserModifiedEvent(User user) {
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE, user.getId(), UserEventType.USER_MODIFIED.name(),
                    Map.of("eventType", UserEventType.USER_MODIFIED.name(), "aggregateId", user.getId(), "data", user.toDto()));
        } else {
            eventPublisher.publish(new UserModifiedEvent(user.toDto()));
        }
    }

    private void validateUpdate(User user, UserUpdateRequest request) {
        if (!user.getNickname().equals(request.nickname())
                && userSupport.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATION);
        }

        if (!user.getPhoneNumber().equals(request.phoneNumber())
                && userSupport.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.PHONE_DUPLICATION);
        }
    }

    private void isActiveUser(User user){
        if(!user.isAvailableUser()) throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN);
    }

}
