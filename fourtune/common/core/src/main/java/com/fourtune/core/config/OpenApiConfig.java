package com.fourtune.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) Configuration
 * API 문서 설정 + JWT 인증 설정
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Fourtune API",
        version = "v1",
        description = "Fourtune 실시간 온라인 경매 플랫폼 API 문서",
        contact = @Contact(
            name = "Fourtune Team",
            email = "support@fourtune.com"
        )
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "JWT 토큰을 입력하세요 (Bearer 접두사 없이 토큰만 입력)"
)
public class OpenApiConfig {
}
