package com.fourtune.auction.boundedContext.payment.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(

        @NotBlank(message = "paymentKey는 필수입니다.")
        String paymentKey,

        @NotBlank(message = "orderId는 필수입니다.")
        Long orderId,

        @NotNull(message = "amount는 필수입니다.")
        Long amount // 토스펭이 결제 금액
) {
}
