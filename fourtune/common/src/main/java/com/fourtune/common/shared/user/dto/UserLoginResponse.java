package com.fourtune.common.shared.user.dto;

public record UserLoginResponse(
        Long userId,
        String email,
        String nickname
) {}
