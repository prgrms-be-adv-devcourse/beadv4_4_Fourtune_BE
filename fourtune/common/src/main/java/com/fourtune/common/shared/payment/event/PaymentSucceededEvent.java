package com.fourtune.common.shared.payment.event;

import com.fourtune.common.shared.auction.dto.OrderDetailResponse;
import com.fourtune.common.shared.payment.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentSucceededEvent {
    private final OrderDto order;
    private final long pgPaymentAmount;
}
