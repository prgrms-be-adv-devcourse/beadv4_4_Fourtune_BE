package com.fourtune.auction.boundedContext.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    BID("입찰 알림"),
    WON("낙찰 알림"),
    PAYMENT("결제 알림"),
    INTEREST("관심상품 알림");

    private final String description;
}
