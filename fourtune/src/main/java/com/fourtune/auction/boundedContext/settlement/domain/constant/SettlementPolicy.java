package com.fourtune.auction.boundedContext.settlement.domain.constant;

import lombok.Getter;

@Getter
public enum SettlementPolicy {
    SETTLEMENT_WAITING_DAYS(0),
    COMMISSION_RATE(10);

    private final int value;

    SettlementPolicy(int value) {
        this.value = value;
    }

}
