package com.fourtune.common.shared.settlement.event;

import com.fourtune.common.shared.settlement.dto.SettlementDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SettlementCompletedEvent {
    SettlementDto settlementDto;
}
