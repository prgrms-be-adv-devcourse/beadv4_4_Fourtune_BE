package com.fourtune.auction.adapter.out.api;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder(FeignErrorDecoder decoder) {
        return decoder;
    }
}
