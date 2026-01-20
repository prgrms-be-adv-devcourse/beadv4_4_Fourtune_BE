package com.fourtune.auction.shared.settlement.event;

import com.fourtune.auction.shared.settlement.dto.SettlementDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SettlementCompletedEvent {
    /**
     * 리스너 TODO
     * settlement -> 새 settlement생성 [v]
     * payment -> cash log 생성 [ ]
     */
    SettlementDto settlementDto;
}
