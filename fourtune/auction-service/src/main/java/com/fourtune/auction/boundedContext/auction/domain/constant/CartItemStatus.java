package com.fourtune.auction.boundedContext.auction.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CartItemStatus {
    ACTIVE("활성", "장바구니에 담긴 상태"),
    PURCHASED("구매완료", "즉시구매하여 Order로 전환됨"),
    EXPIRED("만료", "경매가 종료되어 더이상 즉시구매 불가");

    private final String title;
    private final String description;
}
