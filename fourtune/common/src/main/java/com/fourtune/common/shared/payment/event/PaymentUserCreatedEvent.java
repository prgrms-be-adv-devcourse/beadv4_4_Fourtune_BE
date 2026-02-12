package com.fourtune.common.shared.payment.event;

import com.fourtune.common.shared.payment.dto.PaymentUserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentUserCreatedEvent {
    PaymentUserDto paymentUserDto;
}
