package com.fourtune.auction.shared.user.dto;

import com.fourtune.auction.boundedContext.user.domain.entity.User;

import java.time.LocalDateTime;

public record UserResponse(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, String email, String nickname) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getCreatedAt(), user.getUpdatedAt(), user.getEmail(), user.getNickname());
    }
}
