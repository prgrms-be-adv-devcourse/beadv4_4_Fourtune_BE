package com.fourtune.auction.boundedContext.settlement.adapter.in.web.dto;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SettlementCandidatedItemDto {
    private Long id;
    private SettlementEventType settlementEventType;
    private String relTypeCode;
    private Long relId;
    private LocalDateTime paymentDate;
    private Long amount;

    // 연관 엔티티에서 필요한 데이터만 추출 (프록시 초기화 방지)
    private Long payerId;
    private String payerName;
    private Long payeeId;
    private String payeeName;

    public SettlementCandidatedItemDto(SettlementCandidatedItem item) {
        this.id = item.getId();
        this.settlementEventType = item.getSettlementEventType();
        this.relTypeCode = item.getRelTypeCode();
        this.relId = item.getRelId();
        this.paymentDate = item.getPaymentDate();
        this.amount = item.getAmount();

        // 연관 관계 엔티티가 null이 아닐 때만 안전하게 데이터 추출
        if (item.getPayer() != null) {
            this.payerId = item.getPayer().getId();
            this.payerName = item.getPayer().getNickname();
        }
        if (item.getPayee() != null) {
            this.payeeId = item.getPayee().getId();
            this.payeeName = item.getPayee().getNickname();
        }
    }
}