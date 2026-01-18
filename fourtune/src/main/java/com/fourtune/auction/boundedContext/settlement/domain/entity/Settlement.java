package com.fourtune.auction.boundedContext.settlement.domain.entity;

import com.fourtune.auction.global.common.BaseIdAndTime;
import com.fourtune.auction.shared.settlement.dto.SettlementDto;
import com.fourtune.auction.shared.settlement.event.SettlementCompletedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "SETTLEMENT_SETTLEMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Settlement extends BaseIdAndTime {

    @ManyToOne(fetch = LAZY)
    private SettlementUser payee;

    private LocalDateTime settledAt;

    private Long amount;

    @OneToMany(mappedBy = "settlement", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<SettlementItem> items = new ArrayList<>();

    public Settlement(SettlementUser payee){
        this.payee = payee;
        this.amount = 0L;
    }

    public void competeSettlement(){
        this.settledAt = LocalDateTime.now();
        publishEvent(new SettlementCompletedEvent(
                toDto()
        ));
    }

    public SettlementDto toDto(){
        return SettlementDto.builder()
                .id(getId())
                .settledAt(getSettledAt())
                .amount(getAmount())
                .payeeEmail(getPayee().getEmail())
                .payeeId(getPayee().getId())
                .updatedAt(getUpdatedAt())
                .createdAt(getCreatedAt())
                .build();
    }
}
