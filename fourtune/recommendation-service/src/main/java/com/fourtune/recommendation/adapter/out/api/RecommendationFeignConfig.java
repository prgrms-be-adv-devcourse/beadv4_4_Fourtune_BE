package com.fourtune.recommendation.adapter.out.api;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Recommendation 서비스 전용 Feign 설정.
 * - Authorization 헤더 전파 (JWT 토큰 릴레이)
 * - 에러 핸들링
 *
 * 주의: @Configuration을 붙이면 ComponentScan에 의해 글로벌 등록되어
 * FeignContext 초기화 충돌 발생. @FeignClient(configuration=...)으로만 사용.
 */
@Slf4j
public class RecommendationFeignConfig {

    @Bean
    public RequestInterceptor recommendationRequestInterceptor() {
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

    @Bean
    public ErrorDecoder recommendationErrorDecoder() {
        return (methodKey, response) -> {
            log.warn("[REC][FEIGN] {} → status={}", methodKey, response.status());
            return new RuntimeException("Search API 호출 실패: status=" + response.status());
        };
    }
}
