package com.fourtune.auction.boundedContext.payment.domain.entity;

import com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType;
import com.fourtune.auction.global.common.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;

@Builder
@AllArgsConstructor
@Entity
@Table(name = "PAYMENT_WALLET")
@NoArgsConstructor
@Getter
public class Wallet extends BaseIdAndTime {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Getter
    private long balance;

    @OneToMany(mappedBy = "wallet", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<CashLog> cashLogs = new ArrayList<>();

    public void credit(Long amount, CashEventType cashEventType, String order, Long orderId) {
        addCashLog(amount, cashEventType, order, orderId);

    }

    public void debit(Long price, CashEventType cashEventType, String order, Long orderId) {


    }

    private CashLog addCashLog(long amount, CashEventType cashEventType, String relTypeCode, Long relId){
        CashLog cashLog = new CashLog(
                cashEventType,
                relTypeCode,
                relId,
                user,
                this,
                amount,
                balance
        );

        cashLogs.add(cashLog);

        return cashLog;
    }
}