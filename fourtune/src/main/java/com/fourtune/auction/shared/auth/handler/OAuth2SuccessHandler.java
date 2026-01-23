package com.fourtune.auction.shared.auth.handler;

import com.fourtune.auction.boundedContext.user.application.service.UserSupport;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.security.jwt.JwtTokenProvider;
import com.fourtune.auction.shared.auth.dto.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSupport userSupport;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserContext userContext = (UserContext) authentication.getPrincipal();

        User user = userSupport.findByIdOrThrow(userContext.id());

        String accessToken = jwtTokenProvider.createAccessToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/login-success") // 프론트 주소 확인 필요
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
