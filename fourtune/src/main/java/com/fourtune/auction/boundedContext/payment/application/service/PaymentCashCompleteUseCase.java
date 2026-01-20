package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.shared.payment.dto.OrderDto;
import com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.payment.event.PaymentFailedEvent;
import com.fourtune.auction.shared.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCashCompleteUseCase {

    private final PaymentFacade paymentFacade;
    private final EventPublisher eventPublisher;

    @Transactional
    public void cashComplete(OrderDto orderDto, Long pgAmount) {
        Wallet customerWallet = paymentFacade.findWalletByUserId(orderDto.getUserId()).orElseThrow();
        Wallet systemWallet = paymentFacade.findSystemWallet().orElseThrow();

        if (pgAmount > 0) {
            customerWallet.credit(
                    pgAmount,
                    CashEventType.충전__PG결제_토스페이먼츠,
                    "Order",
                    orderDto.getOrderId()
            );
        }

        boolean canPay = customerWallet.getBalance() >= orderDto.getPrice();

        if (canPay) {
            customerWallet.debit(
                    orderDto.getPrice(),
                    CashEventType.사용__주문결제,
                    "Order",
                    orderDto.getOrderId()
            );

            systemWallet.credit(
                    orderDto.getPrice(),
                    CashEventType.임시보관__주문결제,
                    "Order",
                    orderDto.getOrderId()
            );

            eventPublisher.publish(
                    new PaymentSucceededEvent(
                            orderDto,
                            pgAmount
                    )
            );
        } else {
            eventPublisher.publish(
                    new PaymentFailedEvent(
                            "400-1",
                            "충전은 완료했지만 %번 주문을 결제완료처리를 하기에는 예치금이 부족합니다.".formatted(orderDto.getOrderId()),
                            orderDto,
                            pgAmount,
                            orderDto.getPrice() - customerWallet.getBalance()
                    )
            );
        }
    }
}
