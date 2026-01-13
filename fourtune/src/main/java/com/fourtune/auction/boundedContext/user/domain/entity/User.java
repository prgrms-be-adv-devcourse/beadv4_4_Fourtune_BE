package com.fourtune.auction.boundedContext.user.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User Domain Entity
 * 사용자 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    private Long id;
    private String email;
    private String nickname;
    private String password;
    private String phoneNumber;
    
    // TODO: 비즈니스 로직 추가
}

