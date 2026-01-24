package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.user.dto.UserUpdateRequest;
import com.fourtune.auction.shared.user.event.UserModifiedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserModifiedUseCase {

    private final UserSupport userSupport;
    private final EventPublisher eventPublisher;

    @Transactional
    public void update(Long userId, UserUpdateRequest request){
        User user = userSupport.findByIdOrThrow(userId);

        isActiveUser(user);
        validateUpdate(user, request);

        user.updateProfile(request.nickname(), request.phoneNumber());
        eventPublisher.publish(new UserModifiedEvent(user.toDto()));
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
