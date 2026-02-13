package com.fourtune.common.shared.payment.event;

import com.fourtune.common.shared.auction.dto.OrderDetailResponse;
import com.fourtune.common.shared.payment.dto.OrderDto;
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
