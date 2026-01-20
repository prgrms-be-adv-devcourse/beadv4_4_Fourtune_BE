package com.fourtune.auction.boundedContext.settlement.domain.entity;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.auction.global.common.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
