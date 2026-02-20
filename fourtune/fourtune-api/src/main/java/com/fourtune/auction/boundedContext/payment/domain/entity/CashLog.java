package com.fourtune.auction.boundedContext.payment.domain.entity;

import com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType;
import com.fourtune.core.dto.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "PAYMENT_CASH_LOG")
@NoArgsConstructor
@Getter
public class CashLog extends BaseIdAndTime {

    @Enumerated(EnumType.STRING)
    private CashEventType eventType;

    private String relTypeCode;

    private Long relId;

    @ManyToOne(fetch = LAZY)
    private PaymentUser paymentUser;

    @ManyToOne(fetch = LAZY)
    private Wallet wallet;

    private long amount;

    private long balance;

    public CashLog(CashEventType eventType, String relTypeCode, Long relId, PaymentUser paymentUser, Wallet wallet, Long amount, Long balance) {
        this.eventType = eventType;
        this.relTypeCode = relTypeCode;
        this.relId = relId;
        this.paymentUser = paymentUser;
        this.wallet = wallet;
        this.amount = amount;
        this.balance = balance;
    }
}
