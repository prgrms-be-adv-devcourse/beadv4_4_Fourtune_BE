package com.fourtune.auction.shared.payment.dto;

import lombok.Getter;

@Getter
public class OrderDto {
    private Long orderId;
    private Long price;
    private Long userId;
}
