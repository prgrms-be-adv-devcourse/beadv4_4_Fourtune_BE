package com.fourtune.common.shared.fcmToken.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record FCMTokenRequest(
        @NotBlank String token
) {}
