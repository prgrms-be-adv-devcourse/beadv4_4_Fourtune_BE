package com.fourtune.auction.shared.payment.dto;

import com.fourtune.auction.boundedContext.payment.domain.entity.Refund;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RefundResponse {
    private Long refundId;
    private Long refundAmount;
    private String refundReason;
    private LocalDateTime refundedAt;

    public static RefundResponse from(Refund refund) {
        return RefundResponse.builder()
                .refundId(refund.getId())
                .refundAmount(refund.getCancelAmount())
                .refundReason(refund.getCancelReason())
                .refundedAt(refund.getCreatedAt())
                .build();
    }
}