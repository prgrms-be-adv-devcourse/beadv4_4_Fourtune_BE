package com.fourtune.shared.settlement.event;

import com.fourtune.shared.settlement.dto.SettlementDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SettlementCompletedEvent {
    SettlementDto settlementDto;
}
