package com.fourtune.jwt;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * 각 마이크로서비스가 공통 보안 설정(GlobalJwtSecurityConfig)에
 * 고유한 API 라우팅 규칙(예: public 경로 개방)을 추가할 수 있도록 뚫어놓은 확장점입니다.
 */
public interface SecurityRouteCustomizer {
    void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth);
}
