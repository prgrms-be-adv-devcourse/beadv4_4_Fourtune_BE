package com.fourtune.auction.global.config;

import com.fourtune.auction.boundedContext.auth.application.service.oauth.CustomOAuth2UserService;
import com.fourtune.auction.global.security.jwt.JwtAuthenticationFilter;
import com.fourtune.auction.global.security.jwt.JwtTokenProvider;
import com.fourtune.auction.shared.auth.handler.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration; // [추가]
import org.springframework.web.cors.CorsConfigurationSource; // [추가]
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // [추가]
import java.util.List; // [추가]

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // [1] 여기에 CORS 설정을 반드시 연결해야 합니다! (이게 없어서 302가 떴던 것)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // [팁] Preflight(OPTIONS) 요청은 무조건 허용해주는 것이 안전합니다.
                        .requestMatchers(org.springframework.web.cors.CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs"
                        ,"/swagger-ui.html", "/api/auth/reissue", "/token.html", "/firebase-messaging-sw.js").permitAll()
                        .requestMatchers("/api/auth/**", "/api/users/signup").permitAll()
                        .requestMatchers("/tosspay.html").permitAll()
                        .requestMatchers("/", "/index.html", "/oauth2/**", "/login-success" /*조회 검색 추가*/).permitAll()
                        .requestMatchers("/api/v1/search/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/auctions/*").permitAll() // 비로그인 경매아이템 상세페이지 조회 허용
                        .requestMatchers("/api/v1/orders/*/complete").permitAll() // 결제 완료 콜백 (외부 결제사에서 호출)
                        .requestMatchers("/api/v1/orders/public/**").permitAll() // 주문 조회 (결제 페이지용, 인증 없음)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    // [2] CORS 설정 Bean 추가 (아까 WebConfig에 있던 내용을 여기로 가져옴)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://fourtune.store",
                "https://www.fourtune.store",
                "https://*.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("ETag", "Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
