package com.fourtune.auction.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Configuration
 * JPA 설정
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    // TODO: QueryDSL 설정
    // @Bean
    // public JPAQueryFactory jpaQueryFactory(EntityManager em) {
    //     return new JPAQueryFactory(em);
    // }
}

