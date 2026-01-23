package com.fourtune.auction.shared.payment.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderType;
import com.fourtune.auction.shared.auction.dto.OrderDetailResponse;
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
    private Long orderId;
    private String orderNo;
    private Long price;   // 전체 결제 총액 (Total Amount)
    private Long userId;  // 구매자 ID (Buyer)
    private LocalDateTime paymentDate;

    private List<OrderItem> items;

    private String thumbnailUrl;
    private String winnerNickname;
    private String sellerNickname;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private String paymentKey;
    private LocalDateTime createdAt;


    public static OrderDto from(OrderDetailResponse response) {
        return OrderDto.builder()
                .orderId(response.id())
                .orderNo(response.orderId())
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


    public OrderDetailResponse toOrderDetailResponse() {
        // 아이템 리스트의 첫 번째 요소를 가져옴 (없으면 null)
        OrderItem firstItem = this.items.get(0);

        return new OrderDetailResponse(
                this.orderId,                           // id
                this.orderNo,                           // orderId
                firstItem.getItemId(),                  // auctionId
                firstItem.getItemName(),                // auctionTitle
                this.thumbnailUrl,                                   // thumbnailUrl (정보 없음)
                this.userId,                            // winnerId
                this.winnerNickname,                                   // winnerNickname (정보 없음)
                firstItem.getSellerId(), // sellerId
                this.sellerNickname,                                   // sellerNickname (정보 없음)
                BigDecimal.valueOf(this.price),         // finalPrice
                this.orderType,                                   // OrderType (정보 없음)
                this.orderStatus,                                   // OrderStatus (정보 없음)
                this.paymentKey,                                   // paymentKey (정보 없음)
                this.paymentDate,                       // paidAt
                this.createdAt                     // createdAt (현재 시간)
        );
    }

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
