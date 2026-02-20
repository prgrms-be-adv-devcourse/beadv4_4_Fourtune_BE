package com.fourtune.auction.global.security.jwt;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private com.fourtune.common.global.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 토큰이 헤더에 있으면 인증 객체를 SecurityContext에 저장하고 체인을 진행한다")
    void doFilterInternal_ValidToken() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        String bearerToken = "Bearer " + validToken;

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", bearerToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails principal = new User("1", "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());

        given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
        given(jwtTokenProvider.getAuthentication(validToken)).willReturn(auth);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication actualAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(actualAuth).isNotNull();
        assertThat(actualAuth.getName()).isEqualTo("1");

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰이 없으면 인증 없이 체인을 진행한다")
    void doFilterInternal_NoToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        // 1. 인증 정보가 없어야 함
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // 2. Provider는 호출되지 않았어야 함
        verify(jwtTokenProvider, never()).validateToken(anyString());

        // 3. 다음 필터로는 넘어가야 함 (로그인 안 된 사용자도 접근 가능한 페이지가 있을 수 있으므로)
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰(만료/변조)이면 인증 없이 체인을 진행한다")
    void doFilterInternal_InvalidToken() throws ServletException, IOException {
        String invalidToken = "invalid.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + invalidToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        //  getAuthentication은 호출되면 안 됨 (유효하지 않으므로)
        verify(jwtTokenProvider, never()).getAuthentication(anyString());

        // 다음 필터로 진행
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer 타입이 아닌 헤더는 무시하고 체인을 진행한다")
    void doFilterInternal_NotBearerToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic some_basic_auth_code");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰이 만료되었을 때 401 상태코드와 에러 JSON을 응답한다")
    void doFilterInternal_ExpiredToken_ReturnsErrorResponse() throws Exception {
        String expiredToken = "expired.token.value";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expiredToken);
        request.setRequestURI("/api/boards");

        MockHttpServletResponse response = new MockHttpServletResponse();

        doThrow(new ExpiredJwtException(null, null, "Token Expired"))
                .when(jwtTokenProvider).validateToken(anyString());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(ErrorCode.EXPIRED_ACCESS_TOKEN.getStatus());

        assertThat(response.getContentType()).contains("application/json");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

        String responseBody = response.getContentAsString();

        assertThat(responseBody).contains(ErrorCode.EXPIRED_ACCESS_TOKEN.getCode());
        assertThat(responseBody).contains(ErrorCode.EXPIRED_ACCESS_TOKEN.getMessage());

        verify(filterChain, never()).doFilter(request, response);
    }

}
