package com.fourtune.auction.global.config;

import com.fourtune.auction.global.eventPublisher.EventPublisher;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfig {

    @Getter
    private static EventPublisher eventPublisher;

    @Autowired
    public void setEventPublisher(EventPublisher eventPublisher){
        GlobalConfig.eventPublisher = eventPublisher;
    }

}
