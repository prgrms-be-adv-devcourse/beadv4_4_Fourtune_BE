package com.fourtune.auction.shared.payment.dto;

import com.fourtune.auction.boundedContext.payment.domain.constant.PaymentStatus;
import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.Refund;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 결제 취소 요청에 대한 응답 DTO (Record)
 */
@Builder
public record PaymentRefundResponse(
        Long refundId,            // 환불 이력 ID
        String paymentKey,        // 결제 고유 키 (PG)
        String orderNo,           // 주문 번호
        Long refundAmount,        // 이번에 환불된 금액
        String refundReason,      // 환불 사유
        Long remainBalance,       // 환불 후 남은 결제 잔액
        PaymentStatus paymentStatus, // 현재 결제 상태 (PARTIAL_CANCELED, CANCELED)
        LocalDateTime refundedAt,  // 환불 발생 일시
        String pgTransactionKey    // PG사 환불 키 (있을 경우)
) {
    /**
     * Refund 엔티티를 기반으로 Response 생성
     * Service에서 반환한 Refund 객체 하나만 넣으면 알아서 Payment 정보까지 조합해줍니다.
     */
    public static PaymentRefundResponse from(Refund refund) {
        Payment payment = refund.getPayment();

        return PaymentRefundResponse.builder()
                .refundId(refund.getId())
                .paymentKey(payment.getPaymentKey())
                .orderNo(payment.getOrderNo()) // 주문번호 (필요시 orderId도 추가 가능)
                .refundAmount(refund.getCancelAmount())
                .refundReason(refund.getCancelReason())
                .remainBalance(payment.getBalanceAmount()) // Payment의 현재 잔액
                .paymentStatus(payment.getStatus())       // Payment의 현재 상태
                .refundedAt(refund.getCreatedAt()) // BaseIdAndTime 상속 가정
                .pgTransactionKey(refund.getTransactionKey())
                .build();
    }
}