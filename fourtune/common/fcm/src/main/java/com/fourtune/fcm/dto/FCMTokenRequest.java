package com.fourtune.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record FCMTokenRequest(
        @NotBlank String token
) {}
