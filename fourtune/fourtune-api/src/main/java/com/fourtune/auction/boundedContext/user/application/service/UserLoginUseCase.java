package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.security.jwt.JwtTokenProvider; // (가정) JWT 생성기
import com.fourtune.auction.shared.user.dto.UserLoginRequest;
import com.fourtune.auction.shared.user.dto.UserLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLoginUseCase {

    private final UserSupport userSupport;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public UserLoginResponse userLogin(UserLoginRequest request) {
        User user = userSupport.findActiveUserByEmailOrThrow(request.email());

        validatePassword(request.password(), user.getPassword());

        return new UserLoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

}
