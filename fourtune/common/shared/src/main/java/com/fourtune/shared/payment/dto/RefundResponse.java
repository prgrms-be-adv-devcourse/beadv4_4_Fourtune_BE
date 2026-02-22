package com.fourtune.shared.payment.dto;


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

}
