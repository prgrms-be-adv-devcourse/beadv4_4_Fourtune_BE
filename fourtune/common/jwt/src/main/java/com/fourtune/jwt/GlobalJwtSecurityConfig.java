package com.fourtune.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class GlobalJwtSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    // 마이크로서비스에서 주입(구현)한 라우팅 규칙이 있다면 가져오고, 없으면 빈 상태
    private final Optional<SecurityRouteCustomizer> routeCustomizer;

    /**
     * 기본 보안 필터 체인.
     * 다른 서비스(예: fourtune-api)에서 직접 SecurityFilterChain Bean을 생성하면
     * 이 설정은 자동으로 무시됩니다. (@ConditionalOnMissingBean)
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain globalFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(globalCorsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> {
                    // 1. 공통으로 열어두는 예외 경로 (Preflight 등)
                    auth.requestMatchers(org.springframework.web.cors.CorsUtils::isPreFlightRequest).permitAll();
                    auth.requestMatchers("/actuator/prometheus", "/v3/api-docs", "/v3/api-docs/**").permitAll();

                    // 2. 서비스 고유의 라우팅 확장점이 있다면 조립해줌
                    routeCustomizer.ifPresent(customizer -> customizer.customize(auth));

                    // 3. 나머지는 전부 인증 필요
                    auth.anyRequest().authenticated();
                })

                // 4. JWT 필터 부착
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    public CorsConfigurationSource globalCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // MSA 환경에서는 각 서비스가 와일드카드로 열거나 환경별로 구체화할 수 있도록 * 사용 (Gateway 등에서 제어)
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
