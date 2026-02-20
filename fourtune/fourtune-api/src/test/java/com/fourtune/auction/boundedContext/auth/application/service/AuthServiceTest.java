package com.fourtune.auction.boundedContext.auth.application.service;

import com.fourtune.auction.boundedContext.auth.port.out.RefreshTokenRepository;
import com.fourtune.auction.boundedContext.user.application.service.UserSupport;
import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.mapper.UserMapper;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.security.jwt.JwtTokenProvider;
import com.fourtune.shared.auth.dto.TokenResponse;
import com.fourtune.shared.user.dto.UserLoginRequest;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserSupport userSupport;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private User user;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공 시 액세스 토큰과 리프레시 토큰을 반환한다")
    void login_Success() {
        UserLoginRequest request = new UserLoginRequest("test@test.com", "password123");

        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("encoded_password")
                .role(Role.USER)
                .build();

        given(userSupport.findActiveUserByEmailOrThrow(request.email())).willReturn(user);
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);
        given(jwtTokenProvider.createAccessToken(UserMapper.toDto(user))).willReturn("access.token");
        given(jwtTokenProvider.createRefreshToken(user.getId())).willReturn("refresh.token");

        TokenResponse result = authService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access.token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh.token");

        verify(userSupport).findActiveUserByEmailOrThrow(request.email());
        verify(passwordEncoder).matches("password123", "encoded_password");
        verify(refreshTokenRepository).save(user.getId(), "refresh.token");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
    void login_Fail_UserNotFound() {
        UserLoginRequest request = new UserLoginRequest("unknown@test.com", "1234");
        given(userSupport.findActiveUserByEmailOrThrow(request.email()))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 불일치 시 예외 발생")
    void login_Fail_WrongPassword() {
        UserLoginRequest request = new UserLoginRequest("test@test.com", "wrong_password");
        User user = User.builder().email("test@test.com").password("encoded").build();

        given(userSupport.findActiveUserByEmailOrThrow(request.email())).willReturn(user);
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_NOT_MATCH);
    }

    @Test
    @DisplayName("성공: 리프레시 토큰이 유효하고 Redis와 일치하면 재발급된다")
    void reissue_Success() {
        // given
        String refreshToken = "valid_refresh_token";
        String userIdStr = "1";
        Long userId = 1L;

        given(user.getId()).willReturn(userId);
        given(user.getRole()).willReturn(Role.USER);
        given(user.getStatus()).willReturn(com.fourtune.auction.boundedContext.user.domain.constant.Status.ACTIVE);
        given(user.getEmail()).willReturn("test@test.com");
        given(user.getNickname()).willReturn("테스트");

        given(jwtTokenProvider.getUserIdFromToken(refreshToken)).willReturn(userIdStr);
        given(userSupport.findByIdOrThrow(userId)).willReturn(user);
        given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(refreshToken));

        given(jwtTokenProvider.createAccessToken(UserMapper.toDto(user))).willReturn("new_access");
        given(jwtTokenProvider.createRefreshToken(userId)).willReturn("new_refresh");

        TokenResponse response = authService.reissue(refreshToken);

        assertThat(response.getAccessToken()).isEqualTo("new_access");
        assertThat(response.getRefreshToken()).isEqualTo("new_refresh");

        verify(refreshTokenRepository).save(userId, "new_refresh");
    }

    @Test
    @DisplayName("실패: 리프레시 토큰 자체가 만료되었으면 예외 발생")
    void reissue_Fail_Expired() {
        String expiredToken = "expired_token";

        doThrow(new ExpiredJwtException(null, null, "만료됨"))
                .when(jwtTokenProvider).validateToken(expiredToken);

        assertThatThrownBy(() -> authService.reissue(expiredToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("실패: Redis에 저장된 토큰과 다르면 (탈취 의심) 예외 발생")
    void reissue_Fail_TokenMismatch() {
        String requestToken = "hacker_token";
        String storedToken = "original_token";
        String userIdStr = "1";
        Long userId = 1L;

        given(user.getId()).willReturn(userId);
        given(user.getRole()).willReturn(Role.USER);
        given(user.getStatus()).willReturn(com.fourtune.auction.boundedContext.user.domain.constant.Status.ACTIVE);
        given(user.getEmail()).willReturn("test@test.com");
        given(user.getNickname()).willReturn("테스트");

        given(jwtTokenProvider.getUserIdFromToken(requestToken)).willReturn(userIdStr);
        given(userSupport.findByIdOrThrow(userId)).willReturn(user);
        given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.reissue(requestToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_MISMATCH);

        verify(refreshTokenRepository).deleteByUserId(userId);
    }

}
