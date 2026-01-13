package com.fourtune.auction.boundedContext.cash.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "CASH_CASH_LOG")
@NoArgsConstructor
public class CashLog {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private CashEventType eventType;
    private String relTypeCode;
    private int relId;
    @ManyToOne(fetch = LAZY)
    private CashMember member;
    @ManyToOne(fetch = LAZY)
    private Wallet wallet;
    private long amount;
    private long balance;

    public CashLog(CashEventType eventType, String relTypeCode, int relId, CashMember member, Wallet wallet, long amount, long balance) {
        this.eventType = eventType;
        this.relTypeCode = relTypeCode;
        this.relId = relId;
        this.member = member;
        this.wallet = wallet;
        this.amount = amount;
        this.balance = balance;
    }
}
