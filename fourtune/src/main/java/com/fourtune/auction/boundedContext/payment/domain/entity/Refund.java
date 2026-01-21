package com.fourtune.auction.boundedContext.payment.domain.entity;

import com.fourtune.auction.global.common.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PAYMENT_REFUND")
public class Refund extends BaseIdAndTime {

    // 어떤 결제에 대한 취소인지 연결 (Payment : PaymentCancel = 1 : N)
    // 하나의 결제에 대해 여러 번 부분 취소가 가능하므로 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private Long cancelAmount;  // 이번에 취소한 금액

    @Column(length = 255)
    private String cancelReason; // 취소 사유

    @Column(length = 100)
    private String transactionKey; // PG사에서 발급하는 취소 건에 대한 고유 키 (있을 경우 저장)

    @Builder
    public Refund(Payment payment, Long cancelAmount, String cancelReason, String transactionKey) {
        this.payment = payment;
        this.cancelAmount = cancelAmount;
        this.cancelReason = cancelReason;
        this.transactionKey = transactionKey;
    }

    public static Refund create(Payment payment, Long amount, String reason, String transactionKey) {
        return Refund.builder()
                .payment(payment)
                .cancelAmount(amount)
                .cancelReason(reason)
                .transactionKey(transactionKey)
                .build();
    }
}