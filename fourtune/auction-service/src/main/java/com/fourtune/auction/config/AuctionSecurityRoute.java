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
        // GET 조회만 비로그인 허용 (POST/PUT/DELETE는 인증 필요)
        auth.requestMatchers(HttpMethod.GET, "/api/v1/auctions", "/api/v1/auctions/*").permitAll();
        auth.requestMatchers("/api/v1/search/**").permitAll();
        auth.requestMatchers("/internal/**").permitAll(); // 서비스 간 호출 (탈퇴 시 진행 중 경매 확인 등)
        auth.requestMatchers("/actuator/**").permitAll(); // Docker healthcheck 허용
    }
}
