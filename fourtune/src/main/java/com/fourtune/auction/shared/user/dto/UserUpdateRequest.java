package com.fourtune.auction.shared.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(
        @NotBlank(message = "닉네임은 필수 입력값입니다.")
        String nickname,

        @NotBlank(message = "전화번호는 필수 입력값입니다.")
        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber
) {}
