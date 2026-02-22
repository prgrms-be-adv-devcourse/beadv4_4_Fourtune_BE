package com.fourtune.feign;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign이 classpath에 존재하는 서비스에서 자동으로 활성화됩니다.
 * 모든 Feign 요청에 현재 요청의 Authorization 헤더를 전달합니다.
 * 별도 FeignConfig 없이 build.gradle에 Feign 의존성만 추가하면 동작합니다.
 */
@Configuration
public class FeignJwtAutoConfig {

    @Bean
    @ConditionalOnMissingBean(RequestInterceptor.class)
    public RequestInterceptor jwtRequestInterceptor() {
        return template -> {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                HttpServletRequest request = servletAttrs.getRequest();
                String auth = request.getHeader("Authorization");
                if (auth != null && !auth.isBlank()) {
                    template.header("Authorization", auth);
                }
            }
        };
    }
}
