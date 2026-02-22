package com.fourtune.auction.boundedContext.payment.mapper;

import com.fourtune.auction.boundedContext.payment.domain.entity.Refund;
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
