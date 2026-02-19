package com.fourtune.common.shared.payment.event;

import com.fourtune.common.shared.payment.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class PaymentSucceededEvent {
    private OrderDto order;
    private long pgPaymentAmount;
}
