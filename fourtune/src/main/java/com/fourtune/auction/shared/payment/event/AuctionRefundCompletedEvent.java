package com.fourtune.auction.shared.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionRefundCompletedEvent(
            Long refundId,
            String orderId,
            Long auctionId,
            BigDecimal refundAmount,
            Long userId,
            Long sellerId,
            String orderName,
            String refundReason,
            String paymentKey,
            LocalDateTime refundedAt
    ) {
    }
