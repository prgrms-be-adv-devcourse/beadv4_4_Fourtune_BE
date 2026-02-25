package com.fourtune.auction.adapter.out.api;

import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class FeignConfig {

    /**
     * Feign 타임아웃 설정.
     * getNicknamesByIds() 같은 비핵심 Feign 호출이 REQUIRES_NEW 트랜잭션 안에서
     * 무한 대기하면 DB 커넥션을 점유한 채 스케줄러가 블록된다.
     * connectTimeout: 2s, readTimeout: 3s 로 제한한다.
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                true
        );
    }

    /**
     * 재시도 정책: 100ms 간격, 최대 500ms, 최대 2회.
     * 일시적 네트워크 오류에 대해 재시도 후 빠르게 포기한다.
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 500, 2);
    }

    @Bean
    public ErrorDecoder errorDecoder(FeignErrorDecoder decoder) {
        return decoder;
    }
}
