package com.fourtune.shared.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class RefundRequest {
    @NotNull(message = "주문 ID는 필수입니다.")
    private String orderId;

    @NotNull(message = "경매 주문 ID는 필수입니다.")
    private Long auctionOrderId;

    @Valid
    @NotEmpty(message = "환불할 상품 목록은 비어있을 수 없습니다.")
    private List<RefundItemDto> items;

    private Long cancelAmount; // Null일 경우 전액 환불

    @NotNull(message = "취소 사유는 필수입니다.")
    private String cancelReason;

    @Getter
    public static class RefundItemDto {
        @NotNull(message = "주문 ID는 필수입니다.")
        private String orderId;

        @NotNull(message = "경매 주문 ID는 필수입니다.")
        private Long auctionOrderId;

        private Long cancelAmount; // Null일 경우 전액 환불
    }
}
