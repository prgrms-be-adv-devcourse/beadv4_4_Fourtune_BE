package com.fourtune.auction.boundedContext.payment.adapter.in.event;

import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentUserCreatedEvent {
    PaymentUser paymentUserDto;
}
