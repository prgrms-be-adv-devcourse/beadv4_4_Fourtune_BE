package com.fourtune.auction.boundedContext.settlement.domain.entity;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.core.dto.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "SETTLEMENT_ITEM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SettlementItem extends BaseIdAndTime {

    @ManyToOne(fetch = LAZY)
    private Settlement settlement;

    @Enumerated(EnumType.STRING)
    private SettlementEventType settlementEventType;

    String relTypeCode;

    private Long relId;

    private LocalDateTime paymentDate;

    @ManyToOne(fetch = LAZY)
    private SettlementUser payer;

    @ManyToOne(fetch = LAZY)
    private SettlementUser payee;

    private Long amount;

    @Builder
    public SettlementItem(
            Settlement settlement,
            SettlementEventType settlementEventType,
            String relTypeCode,
            Long relId,
            LocalDateTime paymentDate,
            SettlementUser payer,
            SettlementUser payee,
            Long amount
            ){
        this.settlement = settlement;
        this.settlementEventType = settlementEventType;
        this.relTypeCode = relTypeCode;
        this.relId = relId;
        this.payer = payer;
        this.payee = payee;
        this.amount = amount;
    }
}
