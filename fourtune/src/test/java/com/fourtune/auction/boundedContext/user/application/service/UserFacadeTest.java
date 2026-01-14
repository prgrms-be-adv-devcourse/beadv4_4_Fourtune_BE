package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.auth.application.service.AuthService;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.auth.dto.TokenResponse;
import com.fourtune.auction.shared.user.dto.UserLoginRequest;
import com.fourtune.auction.shared.user.dto.UserLoginResponse;
import com.fourtune.auction.shared.user.dto.UserSignUpRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserFacadeTest {

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("회원가입 시 유저 정보가 DB에 저장되고 비밀번호는 암호화되어야 한다")
    void signupSuccessTest() {
        // 1. Given: 가입 요청 데이터 준비
        UserSignUpRequest request = new UserSignUpRequest(
                "test1@example.com",
                "password123!",
                "테스터",
                "010-1234-5678"
        );

        // 2. When: 회원가입 실행
        userFacade.signup(request);

        entityManager.flush(); // insert 쿼리 강제 실행
        entityManager.clear();

        // 3. Then: 검증
        User savedUser = userRepository.findByEmail("test1@example.com")
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        assertThat(savedUser.getEmail()).isEqualTo("test1@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("테스터");

        // 비밀번호가 평문으로 저장되지 않고 암호화되었는지 확인
        assertThat(passwordEncoder.matches("password123!", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getPassword()).isNotEqualTo("password123!");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 가입하면 예외가 발생해야 한다")
    void signupDuplicateEmailTest() {
        // Given: 이미 한 명이 가입된 상태
        UserSignUpRequest request = new UserSignUpRequest("test2@example.com", "password123!", "테스터", "010-1111-1111");
        userFacade.signup(request);

        // When & Then: 동일한 이메일로 가입 시도 시 예외 발생 검증
        assertThatThrownBy(() -> userFacade.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_DUPLICATION);
    }

    @Test
    @DisplayName("가입된 정보로 로그인하면 액세스 토큰과 리프레시 토큰이 발급된다")
    void loginSuccessTest() {
        // 1. Given: 유저가 이미 가입되어 있어야 함
        UserSignUpRequest signupRequest = new UserSignUpRequest(
                "login@test.com", "password123!", "로그인테스터", "010-1111-1111"
        );
        userFacade.signup(signupRequest);

        // 2. When: 로그인 시도
        UserLoginRequest loginRequest = new UserLoginRequest("login@test.com", "password123!");
        TokenResponse response = authService.login(loginRequest);

        // 3. Then: 검증
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();

        // DB에 리프레시 토큰이 잘 저장되었는지 확인 (UserSupport 로직 검증)
        User user = userRepository.findByEmail("login@test.com").get();
        assertThat(user.getRefreshToken()).isEqualTo(response.getRefreshToken());
    }

}
