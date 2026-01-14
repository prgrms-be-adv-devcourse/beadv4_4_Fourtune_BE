package com.fourtune.auction.shared.user.dto;

import com.fourtune.auction.boundedContext.user.domain.entity.User;

public record UserResponse(Long id, String email, String nickname) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
