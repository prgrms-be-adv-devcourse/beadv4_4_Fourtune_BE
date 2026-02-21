package com.fourtune.oauth.config;

import com.fourtune.jwt.JwtTokenProvider;
import com.fourtune.oauth.handler.OAuth2SuccessHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2 관련 빈(Bean)들을 띄워주는 자동 설정 클래스입니다.
 * 
 * [안전 장치]
 * 1. OAuth2 패키지가 Classpath에 존재하는가? (@ConditionalOnClass)
 * 2. security.oauth.enabled 프로퍼티가 true인가? (@ConditionalOnProperty)
 * 위 조건들이 만족되어야만 메모리에 올라갑니다.
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter")
@ConditionalOnProperty(name = "security.oauth.enabled", havingValue = "true")
public class OAuthSecurityConfig {

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider) {
        return new OAuth2SuccessHandler(jwtTokenProvider);
    }

}
