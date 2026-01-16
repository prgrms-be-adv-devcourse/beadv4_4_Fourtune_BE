package com.fourtune.auction.boundedContext.payment.domain.entity;

import com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType;
import com.fourtune.auction.global.common.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "PAYMENT_CASH_LOG")
@NoArgsConstructor
public class CashLog extends BaseIdAndTime {

    @Enumerated(EnumType.STRING)
    private CashEventType eventType;

    private String relTypeCode;

    private int relId;

    @ManyToOne(fetch = LAZY)
    private CashUser user;

    @ManyToOne(fetch = LAZY)
    private Wallet wallet;

    private long amount;

    private long balance;

    public CashLog(CashEventType eventType, String relTypeCode, int relId, CashUser user, Wallet wallet, long amount, long balance) {
        this.eventType = eventType;
        this.relTypeCode = relTypeCode;
        this.relId = relId;
        this.user = user;
        this.wallet = wallet;
        this.amount = amount;
        this.balance = balance;
    }
}
