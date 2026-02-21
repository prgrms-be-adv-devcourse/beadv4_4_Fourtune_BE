package com.fourtune.shared.settlement.event;

import com.fourtune.shared.settlement.dto.SettlementUserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SettlementUserCreatedEvent {
    SettlementUserDto settlementUserDto;
}
