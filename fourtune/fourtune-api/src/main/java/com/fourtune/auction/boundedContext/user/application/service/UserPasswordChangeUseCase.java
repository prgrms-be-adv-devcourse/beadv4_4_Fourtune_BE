package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.user.dto.UserPasswordChangeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPasswordChangeUseCase {

    private final UserSupport userSupport;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void userChangePassword(Long userId, UserPasswordChangeRequest request) {
        User user = userSupport.findByIdOrThrow(userId);

        isActiveUser(user);
        validateCurrentPassword(request.currentPassword(), user.getPassword());

        if (request.currentPassword().equals(request.newPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        String encodedNewPassword = passwordEncoder.encode(request.newPassword());
        user.changePassword(encodedNewPassword);
    }

    private void validateCurrentPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    private void isActiveUser(User user){
        if(!user.isAvailableUser()) throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN);
    }

}
