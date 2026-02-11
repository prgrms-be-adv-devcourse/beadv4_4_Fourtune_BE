package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.constant.PaymentStatus;
import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
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

    private final PaymentSupport paymentSupport;
    private final EventPublisher eventPublisher;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void cashComplete(OrderDto orderDto, Long pgAmount, String paymentKey) {
        Wallet customerWallet = paymentSupport.findWalletByUserId(orderDto.getUserId()).orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_WALLET_NOT_FOUND)
        );
        Wallet systemWallet = paymentSupport.findSystemWallet().orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_SYSTEM_WALLET_NOT_FOUND)
        );

        if (pgAmount > 0) {
            customerWallet.credit(
                    pgAmount,
                    CashEventType.충전__PG결제_토스페이먼츠,
                    "Order",
                    orderDto.getAuctionOrderId()
            );
        }

        boolean canPay = customerWallet.getBalance() >= orderDto.getPrice();

        if (canPay) {
            customerWallet.debit(
                    orderDto.getPrice(),
                    CashEventType.사용__주문결제,
                    "Order",
                    orderDto.getAuctionOrderId()
            );

            systemWallet.credit(
                    orderDto.getPrice(),
                    CashEventType.임시보관__주문결제,
                    "Order",
                    orderDto.getAuctionOrderId()
            );

            // 결제 정보 저장
            PaymentUser paymentUser = customerWallet.getPaymentUser();
            Payment payment = paymentRepository.save(
                    Payment.builder()
                        .paymentKey(paymentKey)
                        .orderId(orderDto.getOrderId())
                        .auctionOrderId(orderDto.getAuctionOrderId())
                        .paymentUser(paymentUser)
                        .amount(orderDto.getPrice())
                        .pgPaymentAmount(pgAmount)
                        .status(PaymentStatus.APPROVED)
                        .build()
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
                            "충전은 완료했지만 %번 주문을 결제완료처리를 하기에는 예치금이 부족합니다.".formatted(orderDto.getAuctionOrderId()),
                            orderDto,
                            pgAmount,
                            orderDto.getPrice() - customerWallet.getBalance()
                    )
            );
            throw new BusinessException(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
        }
    }
}
