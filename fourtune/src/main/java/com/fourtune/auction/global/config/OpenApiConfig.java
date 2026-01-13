package com.fourtune.auction.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) Configuration
 * API 문서 설정
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Fourtune API")
                .version("v1")
                .description("Fourtune 실시간 온라인 경매 플랫폼 API 문서")
                .contact(new Contact()
                    .name("Fourtune Team")
                    .email("support@fourtune.com")));
    }
}
