package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.user.dto.UserUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserModifiedUseCase {

    private final UserSupport userSupport;

    @Transactional
    public void update(Long userId, UserUpdateRequest request){
        User user = userSupport.findByIdOrThrow(userId);

        validateUpdate(user, request);

        user.updateProfile(request.nickname(), request.phoneNumber());
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

}
