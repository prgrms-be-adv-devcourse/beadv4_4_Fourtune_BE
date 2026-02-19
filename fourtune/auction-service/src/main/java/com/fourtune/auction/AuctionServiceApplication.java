package com.fourtune.auction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.fourtune.auction", "com.fourtune.common"})
@EnableJpaAuditing
@EnableScheduling
@EnableFeignClients(basePackages = "com.fourtune.auction")
@EnableJpaRepositories(basePackages = {"com.fourtune.auction", "com.fourtune.common"})
@EntityScan(basePackages = {"com.fourtune.auction", "com.fourtune.common"})
public class AuctionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionServiceApplication.class, args);
    }
}
