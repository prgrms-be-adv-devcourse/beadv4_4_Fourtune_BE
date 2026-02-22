package com.fourtune.auction.boundedContext.auction.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionStatus {
    SCHEDULED("예정"),
    ACTIVE("진행중"),
    ENDED("종료"),
    SOLD("낙찰 완료"),
    SOLD_BY_BUY_NOW("즉시구매 완료"),
    FAIL("유찰"),           // 낙찰자 미결제 등으로 거래 미성사
    CANCELLED("취소");

    private final String description;
}
