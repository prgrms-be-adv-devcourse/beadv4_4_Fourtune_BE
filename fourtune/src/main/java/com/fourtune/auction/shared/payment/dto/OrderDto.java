package com.fourtune.auction.shared.payment.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.shared.auction.dto.OrderDetailResponse;
import com.fourtune.auction.shared.auction.event.OrderCompletedEvent;
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
    private String orderNo;
    private Long price;   // 전체 결제 총액 (Total Amount)
    private Long userId;  // 구매자 ID (Buyer)

    private List<OrderItem> items;

    private OrderStatus orderStatus;
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
                // event.orderId()는 String(UUID)이므로 orderNo에 매핑
                .orderNo(event.orderId())
                .price(event.amount().longValue())
                .userId(event.winnerId())
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
                .orderId(response.id())
                .orderNo(response.orderId())
                .price(response.finalPrice().longValue()) // BigDecimal -> Long
                .userId(response.winnerId())
                .items(List.of(
                        OrderItem.builder()
                                .itemId(response.auctionId())
                                .sellerId(response.sellerId())
                                .price(response.finalPrice().longValue())
                                .itemName(response.auctionTitle())
                                .build()
                ))
                .orderStatus(response.status())
                .createdAt(response.createdAt())
                .build();
    }
}
