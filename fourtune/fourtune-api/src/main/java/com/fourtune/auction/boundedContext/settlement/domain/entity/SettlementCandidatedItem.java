package com.fourtune.auction.boundedContext.settlement.domain.entity;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.core.dto.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "SETTLEMENT_CANDIDATED_ITEM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SettlementCandidatedItem extends BaseIdAndTime {

    @Enumerated(EnumType.STRING)
    private SettlementEventType settlementEventType;

    String relTypeCode;

    private Long relId;
    private String relNo;

    private LocalDateTime paymentDate;

    @ManyToOne(fetch = LAZY)
    private SettlementUser payer;

    @ManyToOne(fetch = LAZY)
    private SettlementUser payee;

    private Long amount;

    @OneToOne(fetch = LAZY)
    @Setter
    private SettlementItem settlementItem;

    @Builder
    public SettlementCandidatedItem(
            SettlementEventType settlementEventType,
            String relTypeCode,
            Long relId,
            String relNo,
            LocalDateTime paymentDate,
            SettlementUser payer,
            SettlementUser payee,
            Long amount
            ){
        this.amount = amount;
        this.settlementEventType = settlementEventType;
        this.relTypeCode = relTypeCode;
        this.relId = relId;
        this.relNo = relNo;
        this.paymentDate = paymentDate;
        this.payer = payer;
        this.payee = payee;
    }

}
