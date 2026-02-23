package com.fourtune.payment.application.service;

import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.payment.domain.constant.CashEventType;
import com.fourtune.payment.domain.constant.PaymentStatus;
import com.fourtune.payment.domain.entity.Payment;
import com.fourtune.payment.domain.entity.PaymentUser;
import com.fourtune.payment.domain.entity.Wallet;
import com.fourtune.payment.port.out.PaymentRepository;
import com.fourtune.shared.kafka.payment.PaymentEventMapper;
import com.fourtune.shared.payment.dto.OrderDto;
import com.fourtune.shared.payment.event.PaymentFailedEvent;
import com.fourtune.shared.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentCashCompleteUseCase {

        private static final String AGGREGATE_TYPE_PAYMENT = "Payment";

        private final PaymentSupport paymentSupport;
        private final EventPublisher eventPublisher;
        private final PaymentRepository paymentRepository;
        private final OutboxService outboxService;
        private final EventPublishingConfig eventPublishingConfig;

        @Transactional
        public void cashComplete(OrderDto orderDto, Long pgAmount, String paymentKey) {
                Wallet customerWallet = paymentSupport.findWalletByUserIdForUpdate(orderDto.getUserId()).orElseThrow(
                                () -> new BusinessException(ErrorCode.PAYMENT_WALLET_NOT_FOUND));
                Wallet systemWallet = paymentSupport.findSystemWalletForUpdate().orElseThrow(
                                () -> new BusinessException(ErrorCode.PAYMENT_SYSTEM_WALLET_NOT_FOUND));

                if (pgAmount > 0) {
                        customerWallet.credit(
                                        pgAmount,
                                        CashEventType.충전__PG결제_토스페이먼츠,
                                        "Order",
                                        orderDto.getAuctionOrderId());
                }

                boolean canPay = customerWallet.getBalance() >= orderDto.getPrice();

                if (canPay) {
                        customerWallet.debit(
                                        orderDto.getPrice(),
                                        CashEventType.사용__주문결제,
                                        "Order",
                                        orderDto.getAuctionOrderId());

                        systemWallet.credit(
                                        orderDto.getPrice(),
                                        CashEventType.임시보관__주문결제,
                                        "Order",
                                        orderDto.getAuctionOrderId());

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
                                                        .build());

                        PaymentSucceededEvent event = new PaymentSucceededEvent(orderDto, pgAmount);
                        if (eventPublishingConfig.isKafkaEnabled()) {
                                outboxService.append(AGGREGATE_TYPE_PAYMENT, payment.getId(),
                                                PaymentEventMapper.EventType.PAYMENT_SUCCEEDED.name(),
                                                Map.of("eventType",
                                                                PaymentEventMapper.EventType.PAYMENT_SUCCEEDED.name(),
                                                                "aggregateId", payment.getId(), "data", event));
                        }
                        eventPublisher.publish(event);
                } else {
                        long shortfall = orderDto.getPrice() - customerWallet.getBalance();
                        PaymentFailedEvent event = new PaymentFailedEvent(
                                        "400-1",
                                        "충전은 완료했지만 %d번 주문을 결제완료처리를 하기에는 예치금이 부족합니다."
                                                        .formatted(orderDto.getAuctionOrderId()),
                                        orderDto, pgAmount, shortfall);
                        if (eventPublishingConfig.isKafkaEnabled()) {
                                outboxService.append(AGGREGATE_TYPE_PAYMENT, 0L,
                                                PaymentEventMapper.EventType.PAYMENT_FAILED.name(),
                                                Map.of("eventType", PaymentEventMapper.EventType.PAYMENT_FAILED.name(),
                                                                "aggregateId", orderDto.getOrderId(), "data", event));
                        }
                        eventPublisher.publish(event);
                        throw new BusinessException(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
                }
        }
}
