package com.fourtune.payment.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 내부 결제 취소 API 요청 (POST /internal/payments/cancel)
 */
public record CancelPaymentRequest(
        @NotBlank(message = "orderId는 필수입니다.")
        String orderId,

        @NotNull(message = "cancelReason은 필수입니다.")
        String cancelReason,

        Long cancelAmount  // null이면 전액 취소
) {
}
