package com.fourtune.auction.config;

import com.fourtune.jwt.SecurityRouteCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
public class AuctionSecurityRoute implements SecurityRouteCustomizer {

    @Override
    public void customize(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        // 내 경매 목록 조회는 로그인 필수 (permitAll 보다 먼저 선언해야 우선 적용)
        auth.requestMatchers(HttpMethod.GET, "/api/v1/auctions/me").authenticated();
        // GET 조회만 비로그인 허용 (POST/PUT/DELETE는 인증 필요)
        auth.requestMatchers(HttpMethod.GET, "/api/v1/auctions", "/api/v1/auctions/*").permitAll();
        auth.requestMatchers("/api/v1/search/**").permitAll();
        auth.requestMatchers("/api/v1/orders/public/**").permitAll(); // 서비스 간 Feign 호출 (payment-service)
        auth.requestMatchers("/internal/**").permitAll(); // 서비스 간 호출 (탈퇴 시 진행 중 경매 확인 등)
        auth.requestMatchers("/actuator/**").permitAll(); // Docker healthcheck 허용
    }
}
