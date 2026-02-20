package com.fourtune.shared.payment.event;

import com.fourtune.shared.payment.dto.OrderDto;
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
