package com.fourtune.common.shared.settlement.event;

import com.fourtune.common.shared.settlement.dto.SettlementUserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SettlementUserCreatedEvent {
    SettlementUserDto settlementUserDto;
}
