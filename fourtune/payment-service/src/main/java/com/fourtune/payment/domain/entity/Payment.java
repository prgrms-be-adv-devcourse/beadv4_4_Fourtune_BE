package com.fourtune.payment.domain.entity;

import com.fourtune.core.dto.BaseIdAndTime;
import com.fourtune.payment.domain.constant.PaymentStatus;
import com.fourtune.shared.payment.dto.PaymentDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "PAYMENT_PAYMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseIdAndTime {

    @Column(nullable = false, unique = true)
    private String paymentKey; // PG사 결제 고유 키 (Toss paymentKey)

    @Column(nullable = false)
    private String orderId; // Auction 모듈의 주문 UUID

    @Column(nullable = false)
    private Long auctionOrderId; // Auction 모듈의 주문 ID

    @ManyToOne(fetch = FetchType.LAZY)
    private PaymentUser paymentUser; // 구매자 ID (결제 이력 조회용)

    @Column(nullable = false)
    private Long amount; // 최초 결제 금액

    @Column(nullable = true)
    private Long pgPaymentAmount;

    @Column(nullable = false)
    private Long balanceAmount; // 취소 가능 잔액 (부분 취소 시 차감됨)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status; // 결제 상태 (APPROVED, CANCELED, PARTIAL_CANCELED)

    @Column(length = 255)
    private String cancelReason; // (전액) 취소 사유

    @Builder
    public Payment(String paymentKey, String orderId, Long auctionOrderId, PaymentUser paymentUser, Long amount, Long pgPaymentAmount, PaymentStatus status) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.auctionOrderId = auctionOrderId;
        this.paymentUser = paymentUser;
        this.amount = amount;
        this.pgPaymentAmount = pgPaymentAmount;
        this.balanceAmount = amount; // 생성 시 잔액은 결제 금액과 동일
        this.status = status;
    }

    /**
     * 환불(취소) 시 잔액 차감 및 상태 변경 로직
     * @param cancelAmount 취소할 금액
     */
    public void decreaseBalance(Long cancelAmount) {
        long rest = this.balanceAmount - cancelAmount;
        if (rest < 0) {
            throw new IllegalStateException("취소 금액이 잔액보다 큽니다.");
        }

        this.balanceAmount = rest;

        if (this.balanceAmount == 0) {
            this.status = PaymentStatus.CANCELED; // 전액 취소됨
        } else {
            this.status = PaymentStatus.PARTIAL_CANCELED; // 부분 취소됨
        }
    }

    /**
     * 강제 전액 취소 처리 (단순 변심 등 전액 환불 시 사용)
     */
    public void cancel(String reason) {
        this.status = PaymentStatus.CANCELED;
        this.balanceAmount = 0L;
        this.cancelReason = reason;
    }

    public PaymentDto toDto(){
        return PaymentDto.builder()
                .paymentId(getId())
                .paymentKey(paymentKey)
                .orderId(orderId)
                .auctionOrderId(auctionOrderId)
                .userId(this.paymentUser.getId())
                .pgPaymentAmount(pgPaymentAmount)
                .amount(amount)
                .balanceAmount(balanceAmount)
                .status(status.name())
                .cancelReason(cancelReason)
                .build();
    }
}
