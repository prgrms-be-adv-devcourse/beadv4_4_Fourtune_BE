package com.fourtune.shared.user.dto;

public record UserLoginResponse(
        Long userId,
        String email,
        String nickname
) {}
