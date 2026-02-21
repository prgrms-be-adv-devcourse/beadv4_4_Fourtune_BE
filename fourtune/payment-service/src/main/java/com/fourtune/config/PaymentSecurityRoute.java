package com.fourtune.config;

import com.fourtune.jwt.SecurityRouteCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
public class PaymentSecurityRoute implements SecurityRouteCustomizer {

    @Override
    public void customize(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/api/v1/orders/*/complete").permitAll();
        auth.requestMatchers("/api/v1/orders/public/**").permitAll();
    }
}
