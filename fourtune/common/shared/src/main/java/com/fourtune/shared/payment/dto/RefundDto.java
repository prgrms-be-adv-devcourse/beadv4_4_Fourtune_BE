package com.fourtune.shared.payment.dto;

import com.fourtune.shared.payment.event.AuctionRefundCompletedEvent;
import com.fourtune.shared.payment.event.PaymentCanceledEvent;
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

    /**
     * PaymentCanceledEvent → RefundDto 변환.
     * auction-service가 결제 취소 이벤트를 수신한 뒤 ORDER_REFUNDED 이벤트를 발행할 때 사용.
     * refundId는 auction-service에서 알 수 없으므로 null 처리.
     */
    public static RefundDto from(PaymentCanceledEvent event) {
        OrderDto order = event.getOrder();
        if (order == null) {
            return null;
        }

        List<RefundItem> items = order.getItems() == null ? List.of() :
                order.getItems().stream()
                        .map(item -> RefundItem.builder()
                                .itemId(item.getItemId())
                                .sellerId(item.getSellerId())
                                .refundPrice(item.getPrice())
                                .itemName(item.getItemName())
                                .build())
                        .toList();

        return RefundDto.builder()
                .refundId(null)
                .orderId(order.getOrderId())
                .auctionOrderId(order.getAuctionOrderId())
                .refundAmount(event.getCancelAmount())
                .userId(order.getUserId())
                .refundDate(java.time.LocalDateTime.now())
                .items(items)
                .refundReason(event.getCancelReason())
                .paymentKey(order.getPaymentKey())
                .build();
    }
}
