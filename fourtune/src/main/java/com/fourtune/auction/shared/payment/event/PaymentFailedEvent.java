package com.fourtune.auction.shared.payment.event;

import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentFailedEvent {
    private final String resultCode;
    private final String msg;
    private final OrderDto order;
    private final Long pgPaymentAmount;
    private final Long shortfallAmount;
}
