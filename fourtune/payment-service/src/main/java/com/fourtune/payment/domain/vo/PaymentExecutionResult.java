package com.fourtune.payment.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * PG사(토스, 카카오페이 등) 결제 승인 결과를 담는 도메인 객체 (Value Object)
 * 위치: Domain Layer (Entity와 구분되는 순수 값 객체)
 */
@Getter
@AllArgsConstructor
@ToString
public class PaymentExecutionResult {
    private final String paymentKey;
    private final String orderId;
    private final Long amount;
    private final boolean isSuccess;
    private final String failureReason; // 실패 시 사유

    // 성공 시 사용하는 생성자 편의 메서드
    public PaymentExecutionResult(String paymentKey, String orderId, Long amount, boolean isSuccess) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.isSuccess = isSuccess;
        this.failureReason = null;
    }

    public static PaymentExecutionResult success(String paymentKey, String orderId, Long amount) {
        return new PaymentExecutionResult(paymentKey, orderId, amount, true);
    }
}
