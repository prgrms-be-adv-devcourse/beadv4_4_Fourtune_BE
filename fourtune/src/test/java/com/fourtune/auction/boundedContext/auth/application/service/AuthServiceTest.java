package com.fourtune.auction.boundedContext.auth.application.service;

import com.fourtune.auction.boundedContext.user.application.service.UserSupport;
import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.security.jwt.JwtTokenProvider;
import com.fourtune.auction.shared.auth.dto.TokenResponse;
import com.fourtune.auction.shared.user.dto.UserLoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserSupport userSupport;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

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
        given(jwtTokenProvider.createAccessToken(user)).willReturn("access.token");
        given(jwtTokenProvider.createRefreshToken(user.getId())).willReturn("refresh.token");

        TokenResponse result = authService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access.token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh.token");

        verify(userSupport).findActiveUserByEmailOrThrow(request.email());
        verify(passwordEncoder).matches("password123", "encoded_password");
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
}
