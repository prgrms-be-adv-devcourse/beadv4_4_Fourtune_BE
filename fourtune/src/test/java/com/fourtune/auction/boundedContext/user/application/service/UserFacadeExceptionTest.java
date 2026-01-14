package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.user.dto.UserLoginRequest;
import com.fourtune.auction.shared.user.dto.UserSignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserFacadeExceptionTest {

    @Autowired private UserFacade userFacade;
    @Autowired private UserSupport userSupport;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인 시 비밀번호가 틀리면 LOGIN_INPUT_INVALID 예외가 발생한다")
    void login_WrongPassword_Exception() {
        String email = "fail@test.com";
        userFacade.signup(new UserSignUpRequest(email, "correctPassword123!", "010-1111-1111", "테스터"));

        UserLoginRequest loginRequest = new UserLoginRequest(email, "wrongPassword!!!");

        assertThatThrownBy(() -> userFacade.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_NOT_MATCH);
    }

    @Test
    @DisplayName("탈퇴(DELETED) 상태인 회원이 로그인하려 하면 USER_NOT_FOUND 예외가 발생한다")
    void login_DeletedUser_Exception() {
        // Given: 유저 가입 후 상태를 DELETED로 변경
        String email = "deleted@test.com";
        userFacade.signup(new UserSignUpRequest(email, "pw123!", "탈퇴자", "010-2222-2222"));

        User user = userSupport.findActiveUserByEmailOrThrow(email);
        user.withdraw();
        userSupport.save(user);

        UserLoginRequest loginRequest = new UserLoginRequest(email, "pw123!");

        assertThatThrownBy(() -> userFacade.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
