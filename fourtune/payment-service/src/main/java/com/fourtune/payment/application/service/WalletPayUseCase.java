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
import com.fourtune.payment.port.out.AuctionPort;
import com.fourtune.payment.port.out.PaymentRepository;
import com.fourtune.shared.kafka.payment.PaymentEventMapper;
import com.fourtune.shared.payment.dto.OrderDto;
import com.fourtune.shared.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 예치금(지갑 잔액)만으로 결제하는 UseCase.
 * Toss PG 없이 지갑 잔액이 주문금액 이상일 때만 호출 가능하다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletPayUseCase {

    private static final String AGGREGATE_TYPE_PAYMENT = "Payment";

    private final PaymentSupport paymentSupport;
    private final AuctionPort auctionPort;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;
    private final OutboxService outboxService;
    private final EventPublishingConfig eventPublishingConfig;

    @Transactional
    public Payment pay(Long userId, String orderId) {
        OrderDto orderDto = auctionPort.getOrder(orderId);
        if (orderDto == null) {
            throw new BusinessException(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND);
        }

        if (!orderDto.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PAYMENT_PURCHASE_NOT_ALLOWED);
        }

        if (!"PENDING".equals(orderDto.getOrderStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_ORDER_NOT_PENDING);
        }

        paymentRepository.findPaymentByOrderId(orderId).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.APPROVED) {
                throw new BusinessException(ErrorCode.PAYMENT_ALREADY_APPROVED);
            }
        });

        Wallet customerWallet = paymentSupport.findWalletByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_WALLET_NOT_FOUND));
        Wallet systemWallet = paymentSupport.findSystemWalletForUpdate()
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_SYSTEM_WALLET_NOT_FOUND));

        if (customerWallet.getBalance() < orderDto.getPrice()) {
            throw new BusinessException(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
        }

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

        PaymentUser paymentUser = customerWallet.getPaymentUser();
        Payment payment = paymentRepository.save(
                Payment.builder()
                        .paymentKey("WALLET_PAY_" + orderId)
                        .orderId(orderDto.getOrderId())
                        .auctionOrderId(orderDto.getAuctionOrderId())
                        .paymentUser(paymentUser)
                        .amount(orderDto.getPrice())
                        .pgPaymentAmount(0L)
                        .status(PaymentStatus.APPROVED)
                        .build()
        );

        PaymentSucceededEvent event = new PaymentSucceededEvent(orderDto, 0L);
        if (eventPublishingConfig.isKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_PAYMENT, payment.getId(),
                    PaymentEventMapper.EventType.PAYMENT_SUCCEEDED.name(),
                    Map.of(
                            "eventType", PaymentEventMapper.EventType.PAYMENT_SUCCEEDED.name(),
                            "aggregateId", payment.getId(),
                            "data", event
                    )
            );
        }
        eventPublisher.publish(event);

        log.info("지갑 결제 완료: userId={}, orderId={}, amount={}", userId, orderId, orderDto.getPrice());
        return payment;
    }
}
