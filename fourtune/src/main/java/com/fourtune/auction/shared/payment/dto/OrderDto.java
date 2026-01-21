package com.fourtune.auction.shared.payment.dto;

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
public class OrderDto {
    private Long orderId;
    private Long price;   // 전체 결제 총액 (Total Amount)
    private Long userId;  // 구매자 ID (Buyer)
    private LocalDateTime paymentDate;

    private List<OrderItem> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long itemId;    // 경매 상품 ID
        private Long sellerId;  // 판매자 ID
        private Long price;     // 개별 상품 가격
        private String itemName; // (옵션) 나중에 로그 찍을 때 편함
    }
}
