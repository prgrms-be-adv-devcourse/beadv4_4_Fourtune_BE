package com.fourtune.auction.shared.payment.event;

import com.fourtune.auction.shared.auction.dto.OrderDetailResponse;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentFailedEvent {
    private final OrderDto order;
    private final String msg;
    private final String resultCode;
    private final Long shortFallAmount;
}
