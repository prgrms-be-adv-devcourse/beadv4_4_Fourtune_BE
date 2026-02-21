package com.fourtune.auction.boundedContext.auction.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    
    PENDING("결제 대기", "낙찰 후 결제 대기 중"),
    COMPLETED("결제 완료", "결제가 완료되어 거래 종료"),
    CANCELLED("주문 취소", "주문이 취소됨");
    
    private final String displayName;
    private final String description;
}
