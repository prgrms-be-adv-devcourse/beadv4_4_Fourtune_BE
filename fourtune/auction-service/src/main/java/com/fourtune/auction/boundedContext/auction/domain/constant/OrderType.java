package com.fourtune.auction.boundedContext.auction.domain.constant;

/**
 * 주문 유형
 */
public enum OrderType {
    AUCTION_WIN("경매 낙찰"),
    BUY_NOW("즉시구매");

    private final String description;

    OrderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
