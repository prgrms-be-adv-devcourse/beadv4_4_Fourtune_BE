package com.fourtune.auction.boundedContext.auth.application.service;

import com.fourtune.auction.boundedContext.user.application.service.UserSupport;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.security.jwt.JwtTokenProvider;
import com.fourtune.auction.shared.auth.dto.TokenResponse;
import com.fourtune.auction.shared.user.dto.UserLoginRequest;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserSupport userSupport;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse login(UserLoginRequest request) {
        User user = userSupport.findActiveUserByEmailOrThrow(request.email());

        validatePassword(request.password(), user.getPassword());

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);

        return TokenResponse.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse reissue(String refreshToken){
        validateToken(refreshToken);

        String id = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userSupport.findByIdOrThrow(Long.parseLong(id));

        isCorrectRequestRefreshToken(user, refreshToken);

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        user.updateRefreshToken(newRefreshToken);

        return new TokenResponse("Bearer", newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userSupport.findByIdOrThrow(userId);

        user.updateRefreshToken(null);
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    private void validateToken(String refreshToken){
        try{
            jwtTokenProvider.validateToken(refreshToken);
        }
        catch(ExpiredJwtException e){
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private void isCorrectRequestRefreshToken(User user, String refreshToken){
        String currentDbToken = user.getRefreshToken();

        if (currentDbToken == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!currentDbToken.equals(refreshToken)) {
            log.warn("🚨 탈취 의심 감지! 해당 계정의 리프레시 토큰을 파기합니다. ID: " + user.getId());

            user.updateRefreshToken(null);

            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
    }

}
