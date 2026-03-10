package com.fourtune.payment.adapter.in.web.dto;

import com.fourtune.payment.domain.vo.PaymentExecutionResult;
import lombok.Builder;

@Builder
public record PaymentConfirmResponse(
        String paymentKey,
        String orderId,
        Long amount,
        boolean success
) {
    public static PaymentConfirmResponse from(PaymentExecutionResult result) {
        return PaymentConfirmResponse.builder()
                .paymentKey(result.getPaymentKey())
                .orderId(result.getOrderId())
                .amount(result.getAmount())
                .success(result.isSuccess())
                .build();
    }
}
