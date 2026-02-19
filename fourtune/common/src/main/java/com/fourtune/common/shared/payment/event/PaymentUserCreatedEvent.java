package com.fourtune.common.shared.payment.event;

import com.fourtune.common.shared.payment.dto.PaymentUserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class PaymentUserCreatedEvent {
    PaymentUserDto paymentUserDto;
}
