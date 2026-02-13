package com.fourtune.common.shared.payment.dto;

import com.fourtune.auction.shared.payment.event.AuctionRefundCompletedEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDto {
    private Long refundId;
    private String orderId;
    private Long auctionOrderId;
    private Long refundAmount;
    private Long userId;
    private LocalDateTime refundDate;

    private List<RefundItem> items;

    private String refundReason;
    private String paymentKey;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundItem {
        private Long itemId;
        private Long sellerId;
        private Long refundPrice;
        private String itemName;
    }

    public static RefundDto from(AuctionRefundCompletedEvent event) {
        return RefundDto.builder()
                .refundId(event.refundId())
                .orderId(event.orderId())
                .refundAmount(event.refundAmount().longValue())
                .userId(event.userId())
                .refundDate(event.refundedAt())
                .items(List.of(
                        RefundItem.builder()
                                .itemId(event.auctionId())
                                .sellerId(event.sellerId())
                                .refundPrice(event.refundAmount().longValue())
                                .itemName(event.orderName())
                                .build()
                ))
                .refundReason(event.refundReason())
                .paymentKey(event.paymentKey())
                .build();
    }
}
