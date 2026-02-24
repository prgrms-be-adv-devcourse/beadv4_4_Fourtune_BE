package com.fourtune.shared.payment.event;

import com.fourtune.shared.payment.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PaymentCanceledEvent {
    private OrderDto order;
    private String cancelReason;
    private Long cancelAmount;
}
