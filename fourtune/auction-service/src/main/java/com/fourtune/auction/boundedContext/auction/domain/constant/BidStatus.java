package com.fourtune.auction.boundedContext.auction.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BidStatus {
    
    ACTIVE("진행중", "경매 진행 중인 입찰"),
    SUCCESS("낙찰", "낙찰된 입찰"),
    FAILED("패찰", "낙찰되지 못한 입찰"),
    CANCELLED("취소", "사용자가 취소한 입찰");
    
    private final String displayName;
    private final String description;
}
