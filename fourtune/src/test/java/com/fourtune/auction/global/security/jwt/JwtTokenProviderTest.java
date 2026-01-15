package com.fourtune.auction.global.security.jwt;

import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "sdsdsdsaWQUIQB1541KJSABDAJKSD1K24JBJ1T14GSF=";
    private final long expiredAccessTokenInMilliseconds = 3600000;
    private final long xpiredRefreshTokenInMilliseconds = 3600000 * 24 * 14;

    @BeforeEach
    void setUp(){
        jwtTokenProvider = new JwtTokenProvider(secretKey, expiredAccessTokenInMilliseconds, xpiredRefreshTokenInMilliseconds);
    }

    @Test
    @DisplayName("액세스, 리프레시 토큰 생성 및 검증 성공")
    void createAndValidateToken() {
        Long userId = 1L;
        Role role = Role.USER;

        String accessToken = jwtTokenProvider.createAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();

        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateExpiredToken() {
        JwtTokenProvider jwtTokenProviderHasZero = new JwtTokenProvider(secretKey, 0, 0);
        String expiredToken = jwtTokenProviderHasZero.createAccessToken(1L, Role.USER);

        boolean isValid = jwtTokenProviderHasZero.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰에서 정보 추출")
    void getAuthentication() {
        String token = jwtTokenProvider.createAccessToken(1L, Role.USER);
        Authentication auth = jwtTokenProvider.getAuthentication(token);

        assertThat(auth.getName()).isEqualTo("1");
        assertThat(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))).isTrue();

    }

}
