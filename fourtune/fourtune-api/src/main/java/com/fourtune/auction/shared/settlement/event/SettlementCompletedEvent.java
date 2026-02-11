package com.fourtune.auction.shared.settlement.event;

import com.fourtune.auction.shared.settlement.dto.SettlementDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SettlementCompletedEvent {
    SettlementDto settlementDto;
}
