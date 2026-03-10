package com.fourtune.payment.adapter.in.web.dto;

import com.fourtune.payment.domain.entity.Refund;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RefundResponse(
        Long id,
        String orderId,
        Long cancelAmount,
        String cancelReason,
        String transactionKey,
        LocalDateTime createdAt
) {
    public static RefundResponse from(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .orderId(refund.getPayment().getOrderId())
                .cancelAmount(refund.getCancelAmount())
                .cancelReason(refund.getCancelReason())
                .transactionKey(refund.getTransactionKey())
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
