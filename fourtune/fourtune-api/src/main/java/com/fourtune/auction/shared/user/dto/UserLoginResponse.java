package com.fourtune.auction.shared.user.dto;

public record UserLoginResponse(
        Long userId,
        String email,
        String nickname
) {}
