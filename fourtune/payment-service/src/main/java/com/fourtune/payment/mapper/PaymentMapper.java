package com.fourtune.payment.mapper;

import com.fourtune.payment.domain.entity.Refund;
import com.fourtune.shared.payment.dto.RefundResponse;

public class PaymentMapper {

    public static RefundResponse from(Refund refund) {
        return RefundResponse.builder()
                .refundId(refund.getId())
                .refundAmount(refund.getCancelAmount())
                .refundReason(refund.getCancelReason())
                .refundedAt(refund.getCreatedAt())
                .build();
    }

}
