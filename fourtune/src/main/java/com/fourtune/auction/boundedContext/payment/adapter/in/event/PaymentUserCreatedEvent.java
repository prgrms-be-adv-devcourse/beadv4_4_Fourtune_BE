package com.fourtune.auction.boundedContext.payment.adapter.in.event;

import com.fourtune.auction.boundedContext.payment.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentUserCreatedEvent {
    User userDto;
}
