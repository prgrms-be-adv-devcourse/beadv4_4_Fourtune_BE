package com.fourtune.recommendation.config;

import com.fourtune.jwt.SecurityRouteCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * 추천 서비스 보안 라우팅 설정.
 * GlobalJwtSecurityConfig의 SecurityRouteCustomizer 확장점을 통해
 * public 경로를 등록.
 */
@Configuration
public class RecommendationSecurityRoute implements SecurityRouteCustomizer {

    @Override
    public void customize(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        // 인기 추천 (비로그인 허용)
        auth.requestMatchers("/api/v1/recommendations/popular").permitAll();
    }
}
