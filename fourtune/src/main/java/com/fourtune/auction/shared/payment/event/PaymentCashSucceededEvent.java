package com.fourtune.auction.shared.payment.event;

import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCashSucceededEvent {
    private final OrderDto order;
    private final long pgPaymentAmount;
}
