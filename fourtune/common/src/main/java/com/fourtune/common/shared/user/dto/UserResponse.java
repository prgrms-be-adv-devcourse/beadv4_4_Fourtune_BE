package com.fourtune.common.shared.user.dto;

import com.fourtune.common.shared.auth.dto.UserContext;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, String email, String nickname, String status, String role) {

    public static UserResponse from(UserContext userContext){
        return UserResponse.builder()
                .id(userContext.id())
                .role(userContext.getRole())
                .build();
    }

}
