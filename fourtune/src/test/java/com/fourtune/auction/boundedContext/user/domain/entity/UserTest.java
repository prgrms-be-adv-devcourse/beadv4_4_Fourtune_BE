package com.fourtune.auction.boundedContext.user.domain.entity;

import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class UserTest {

    @Test
    @DisplayName("프로필 정보를 수정하면 닉네임과 전화번호가 변경되어야 한다")
    void updateProfileTest(){
        User user = User.builder()
                .nickname("옛날이름")
                .phoneNumber("010-1111-1111")
                .build();

        user.updateProfile("옛날이름", "010-2222-2222");

        assert user.getNickname().equals("옛날이름");
        assert user.getPhoneNumber().equals("010-2222-2222");
    }

    @Test
    @DisplayName("회원 탈퇴 시 상태는 SUSPENDED가 되고 탈퇴 일시가 기록되어야 한다")
    void withdrawTest() {
        User user = User.builder()
                .status(Status.ACTIVE)
                .build();

        user.withdraw();

        assertThat(user.getStatus()).isEqualTo(Status.SUSPENDED);
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.isAvailableUser()).isFalse();
    }

    @Test
    @DisplayName("리프레시 토큰이 정상적으로 업데이트 되어야 한다")
    void updateRefreshTokenTest() {
        User user = User.builder().build();
        String newToken = "new-refresh-token-123";

        user.updateRefreshToken(newToken);

        assertThat(user.getRefreshToken()).isEqualTo(newToken);
    }

    @Test
    @DisplayName("사용자 상태가 ACTIVE일 때만 isAvailableUser가 true를 반환한다")
    void isAvailableUserTest() {
        User activeUser = User.builder().status(Status.ACTIVE).build();
        User suspendedUser = User.builder().status(Status.SUSPENDED).build();

        assertThat(activeUser.isAvailableUser()).isTrue();
        assertThat(suspendedUser.isAvailableUser()).isFalse();
    }

}
