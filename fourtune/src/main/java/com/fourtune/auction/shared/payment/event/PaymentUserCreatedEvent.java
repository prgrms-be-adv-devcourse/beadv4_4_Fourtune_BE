package com.fourtune.auction.shared.payment.event;

import com.fourtune.auction.shared.payment.dto.PaymentUserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentUserCreatedEvent {
    PaymentUserDto paymentUserDto;
}
