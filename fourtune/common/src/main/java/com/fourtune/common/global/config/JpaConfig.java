package com.fourtune.common.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Configuration
 * JPA 설정
 */
@Configuration
public class JpaConfig {
    
    // TODO: QueryDSL 설정
    // @Bean
    // public JPAQueryFactory jpaQueryFactory(EntityManager em) {
    //     return new JPAQueryFactory(em);
    // }
}

