package com.fourtune.shared.payment.event;

import com.fourtune.shared.payment.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PaymentFailedEvent {
    private String resultCode;
    private String msg;
    private OrderDto order;
    private Long pgPaymentAmount;
    private Long shortfallAmount;
}
