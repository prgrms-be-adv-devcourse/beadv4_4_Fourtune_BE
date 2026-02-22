package com.fourtune.payment.domain.entity;

import com.fourtune.core.dto.BaseIdAndTime;
import com.fourtune.payment.domain.constant.CashEventType;
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
    private PaymentUser paymentUser;

    @Getter
    private Long balance;

    @Builder.Default
    @OneToMany(mappedBy = "wallet", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<CashLog> cashLogs = new ArrayList<>();

    public void credit(Long amount, CashEventType cashEventType, String relTypeCode, Long relId) {
        balance += amount;
        addCashLog(amount, cashEventType, relTypeCode, relId);
    }

    public void debit(Long amount, CashEventType cashEventType, String relTypeCode, Long relId) {
        balance -= amount;
        addCashLog(-amount, cashEventType, relTypeCode, relId);
    }

    private CashLog addCashLog(long amount, CashEventType cashEventType, String relTypeCode, Long relId){
        CashLog cashLog = new CashLog(
                cashEventType,
                relTypeCode,
                relId,
                paymentUser,
                this,
                amount,
                balance
        );

        cashLogs.add(cashLog);

        return cashLog;
    }
}
