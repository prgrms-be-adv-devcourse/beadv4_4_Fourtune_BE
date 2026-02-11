package com.fourtune.auction.shared.payment.dto;

import com.fourtune.auction.boundedContext.payment.domain.constant.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PaymentDto {
    private Long paymentId;
    private String paymentKey; // PG사 결제 고유 키 (Toss paymentKey)
    private String orderId; // Auction 모듈의 주문 UUID
    private Long auctionOrderId; // Auction 모듈의 주문 ID
    private Long userId; // 구매자 ID (결제 이력 조회용)
    private Long amount; // 최초 결제 금액
    private Long balanceAmount; // 취소 가능 잔액 (부분 취소 시 차감됨)
    private Long pgPaymentAmount; // pg결제 금액
    private String status; // 결제 상태 (APPROVED, CANCELED, PARTIAL_CANCELED)
    private String cancelReason; // (전액) 취소 사유
}
