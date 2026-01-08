package com.fourtune.auction.shared.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User DTO (Shared)
 * 다른 도메인에서 참조할 사용자 DTO
 */
@Getter
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    private String email;
    private String nickname;
    
    // TODO: 필요한 필드 추가
}

