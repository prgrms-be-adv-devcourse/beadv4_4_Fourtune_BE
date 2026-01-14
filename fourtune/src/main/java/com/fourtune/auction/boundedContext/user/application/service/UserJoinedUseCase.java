package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.user.dto.UserSignUpRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserJoinedUseCase {

    private final UserSupport userSupport;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void userJoin(UserSignUpRequest request) {
        validateSignUp(request);

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .phoneNumber(request.phoneNumber())
                .password(encodedPassword)
                .email(request.email())
                .nickname(request.nickname())
                .build();

        userSupport.save(user);
    }

    private void validateSignUp(UserSignUpRequest request) {
        if (userSupport.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);
        }

        if (userSupport.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATION);
        }

        if (userSupport.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.PHONE_DUPLICATION);
        }
    }

}
