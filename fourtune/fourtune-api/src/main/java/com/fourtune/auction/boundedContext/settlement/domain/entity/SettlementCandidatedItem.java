package com.fourtune.auction.boundedContext.settlement.domain.entity;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.core.dto.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(
    name = "SETTLEMENT_CANDIDATED_ITEM",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_settlement_candidated_item_rel",
        // [DEFECT-002 수정] Kafka at-least-once 중복 이벤트 방어.
        // 동일 주문(relNo) + 동일 정산유형(settlementEventType) 조합의 중복 INSERT를 DB 레벨에서 차단한다.
        columnNames = {"rel_no", "settlement_event_type"}
    )
)
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
