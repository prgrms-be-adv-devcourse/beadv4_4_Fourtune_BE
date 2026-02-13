package com.fourtune.common.shared.payment.dto;

import com.fourtune.common.shared.auction.dto.OrderDetailResponse;
import com.fourtune.common.shared.auction.event.OrderCompletedEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long auctionOrderId;
    private String orderId;
    private Long price;   // 전체 결제 총액 (Total Amount)
    private Long userId;  // 구매자 ID (Buyer)
    private LocalDateTime paymentDate;

    private List<OrderItem> items;

    private String thumbnailUrl;
    private String winnerNickname;
    private String sellerNickname;
    private String orderType;
    private String orderStatus;
    private String paymentKey;
    private LocalDateTime createdAt;

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

    /**
     * OrderCompletedEvent -> OrderDto 변환 메서드
     * (이벤트에 없는 필드는 null 처리됩니다)
     */
    public static OrderDto from(OrderCompletedEvent event) {
        return OrderDto.builder()
                // event.orderId()는 String(UUID)
                .orderId(event.orderId())
                .price(event.amount().longValue())
                .userId(event.winnerId())
                .paymentDate(event.paidAt())
                .items(List.of(
                        OrderItem.builder()
                                .itemId(event.auctionId())// order item id x, 일단 auction id로
                                .sellerId(event.sellerId())
                                .price(event.amount().longValue())
                                .itemName(event.orderName())
                                .build()
                ))
                .build();
    }


    public static OrderDto from(OrderDetailResponse response) {
        return OrderDto.builder()
                .auctionOrderId(response.id())
                .orderId(response.orderId())
                .price(response.finalPrice().longValue()) // BigDecimal -> Long
                .userId(response.winnerId())
                .paymentDate(response.paidAt())
                .items(List.of(
                        OrderItem.builder()
                                .itemId(response.auctionId())
                                .sellerId(response.sellerId())
                                .price(response.finalPrice().longValue())
                                .itemName(response.auctionTitle())
                                .build()
                ))
                .thumbnailUrl(response.thumbnailUrl())
                .winnerNickname(response.winnerNickname())
                .sellerNickname(response.sellerNickname())
                .orderType(response.orderType())
                .orderStatus(response.status())
                .paymentKey(response.paymentKey())
                .createdAt(response.createdAt())
                .build();
    }


    /**
     * OrderDto -> OrderDetailResponse 변환 메서드
     * items가 null이거나 비어있을 경우, 관련 필드에 null을 할당하여 NPE 방지
     */
    public OrderDetailResponse toOrderDetailResponse() {
        // 1. 아이템 리스트 안전하게 가져오기
        OrderItem firstItem = (this.items != null && !this.items.isEmpty()) ? this.items.get(0) : null;

        return new OrderDetailResponse(
                this.auctionOrderId,
                this.orderId,
                (firstItem != null) ? firstItem.getItemId() : null,   // auctionId
                (firstItem != null) ? firstItem.getItemName() : null, // auctionTitle
                this.thumbnailUrl,
                this.userId,
                this.winnerNickname,
                (firstItem != null) ? firstItem.getSellerId() : null, // sellerId
                this.sellerNickname,
                (this.price != null) ? BigDecimal.valueOf(this.price) : null, // finalPrice
                this.orderType,
                this.orderStatus,
                this.paymentKey,
                this.paymentDate,
                this.createdAt
        );
    }

}
