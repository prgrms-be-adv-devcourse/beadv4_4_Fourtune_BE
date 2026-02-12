package com.fourtune.auction;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = "com.fourtune")
@EntityScan(basePackages = "com.fourtune")
@SpringBootApplication(scanBasePackages = "com.fourtune")
public class FourtuneApplication {
	public static void main(String[] args) {
		SpringApplication.run(FourtuneApplication.class, args);
	}

	@PostConstruct
	public void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}
}

