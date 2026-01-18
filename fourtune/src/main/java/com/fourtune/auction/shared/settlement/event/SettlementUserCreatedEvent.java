package com.fourtune.auction.shared.settlement.event;

import com.fourtune.auction.shared.settlement.dto.SettlementUserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SettlementUserCreatedEvent {
    SettlementUserDto settlementUserDto;
}
