package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.common.global.error.ErrorCode;
import com.fourtune.common.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Transactional
class UserSupportTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSupport userSupport;

    @Test
    @DisplayName("비활성화된 유저를 이메일로 조회하면 USER_NOT_FOUND 예외가 발생한다")
    void findActiveUser_Fail_Suspended() {
        String email = "test@test.com";
        User suspendedUser = User.builder()
                .email(email)
                .status(Status.SUSPENDED)
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(suspendedUser));

        assertThatThrownBy(() -> userSupport.findActiveUserByEmailOrThrow(email))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
