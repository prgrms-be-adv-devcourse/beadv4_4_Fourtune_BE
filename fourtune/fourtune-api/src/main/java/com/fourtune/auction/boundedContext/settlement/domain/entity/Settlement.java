package com.fourtune.auction.boundedContext.settlement.domain.entity;

import com.fourtune.auction.boundedContext.settlement.adapter.in.web.dto.SettlementResponse;
import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.core.dto.BaseIdAndTime;
import com.fourtune.shared.settlement.dto.SettlementDto;
import com.fourtune.shared.settlement.event.SettlementCompletedEvent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "SETTLEMENT_SETTLEMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // Builder 사용 시 모든 필드 생성자가 필요함
@Builder // 빌더 패턴 추가
@Getter
public class Settlement extends BaseIdAndTime {

    @ManyToOne(fetch = LAZY)
    private SettlementUser payee;

    private LocalDateTime settledAt;

    private Long amount;

    @Builder.Default // 빌더 사용 시에도 new ArrayList<>() 초기화가 적용되도록 설정
    @OrderBy("paymentDate DESC, id DESC")
    @OneToMany(mappedBy = "settlement", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<SettlementItem> items = new ArrayList<>();

    // 기존 생성자 유지 (비즈니스 로직 등에서 사용)
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

    public SettlementItem addItem(SettlementEventType settlementEventType,
                                  String relTypeCode,
                                  Long relId,
                                  LocalDateTime paymentDate,
                                  SettlementUser payer,
                                  SettlementUser payee,
                                  Long amount){

        SettlementItem item = SettlementItem.builder()
                .settlement(this)
                .settlementEventType(settlementEventType)
                .relTypeCode(relTypeCode)
                .relId(relId)
                .paymentDate(paymentDate)
                .payee(payee)
                .payer(payer)
                .amount(amount)
                .build();

        this.items.add(item);
        this.amount += amount;

        return item;
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

    public SettlementResponse toResponse(){
        List<SettlementResponse.Item> itemDtos = this.items.stream()
                .map(item -> SettlementResponse.Item.builder()
                        .itemId(item.getId())
                        .eventType(item.getSettlementEventType().name())
                        .relTypeCode(item.getRelTypeCode())
                        .relId(item.getRelId())
                        .amount(item.getAmount())
                        // payer가 지연 로딩이므로 트랜잭션 내에서 접근해야 함. 없을 경우 대비 null 처리
                        .payerName(item.getPayer() != null ? item.getPayer().getNickname() : null)
                        .paymentDate(item.getPaymentDate())
                        .build())
                .collect(Collectors.toList());

        return SettlementResponse.builder()
                .id(getId())
                .payeeId(getPayee().getId())
                .payeeEmail(getPayee().getEmail())
                .totalAmount(getAmount())
                .settledAt(getSettledAt())
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .items(itemDtos)
                .build();
    }
}
