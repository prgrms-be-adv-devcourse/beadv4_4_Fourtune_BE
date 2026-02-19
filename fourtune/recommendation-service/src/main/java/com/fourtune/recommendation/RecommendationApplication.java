package com.fourtune.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;

@SpringBootApplication(scanBasePackages = {
        "com.fourtune.recommendation",
        "com.fourtune.common"
})
@EnableJpaAuditing
@EnableScheduling
@EnableFeignClients(basePackages = {
        "com.fourtune.recommendation",
        "com.fourtune.common"
})
@EnableJpaRepositories(basePackages = {
        "com.fourtune.recommendation",
        "com.fourtune.common"
})
@EntityScan(basePackages = {
        "com.fourtune.recommendation",
        "com.fourtune.common"
})
public class RecommendationApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendationApplication.class, args);
    }

    // Common 모듈의 SecurityConfig에서 필요한 빈 등록 (런타임 에러 해결용)
    @Bean
    public DefaultOAuth2UserService defaultOAuth2UserService() {
        return new DefaultOAuth2UserService();
    }
}
