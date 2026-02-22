package com.fourtune.core.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer 메트릭 설정
 *
 * @Timed 어노테이션 활성화를 위한 설정
 */
@Configuration
public class MetricsConfig {

    /**
     * @Timed 어노테이션을 사용하기 위한 TimedAspect 빈 등록
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
