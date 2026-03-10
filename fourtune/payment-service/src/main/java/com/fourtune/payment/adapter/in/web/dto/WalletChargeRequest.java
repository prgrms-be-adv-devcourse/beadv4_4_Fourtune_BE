package com.fourtune.payment.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WalletChargeRequest(
        @NotNull(message = "paymentKey는 필수입니다.")
        String paymentKey,

        @NotNull(message = "orderId는 필수입니다.")
        String orderId,

        @NotNull
        @Min(value = 1000, message = "최소 충전금액은 1,000원입니다.")
        @Max(value = 5_000_000, message = "1회 최대 충전금액은 5,000,000원입니다.")
        Long amount
) {}
