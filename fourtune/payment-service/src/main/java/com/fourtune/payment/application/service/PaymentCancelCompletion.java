package com.fourtune.payment.application.service;

import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.payment.domain.constant.CashEventType;
import com.fourtune.payment.domain.entity.*;
import com.fourtune.payment.port.out.PaymentRepository;
import com.fourtune.payment.port.out.RefundRepository;
import com.fourtune.shared.kafka.payment.PaymentEventMapper;
import com.fourtune.shared.payment.dto.OrderDto;
import com.fourtune.shared.payment.event.PaymentCanceledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 결제 취소 DB 처리 전용. 락·지갑 입출금·저장·이벤트를 한 트랜잭션으로 수행.
 * PaymentCancelUseCase에서 PG 호출 후 이 컴포넌트를 호출해 트랜잭션 경계를 분리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelCompletion {

    private static final String AGGREGATE_TYPE_PAYMENT = "Payment";

    private final PaymentSupport paymentSupport;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;
    private final OutboxService outboxService;
    private final EventPublishingConfig eventPublishingConfig;

    @Transactional
    public Refund completeCancelInDb(Payment payment, OrderDto orderDto, Long requestAmount, String cancelReason) {
        Wallet payerWallet = paymentSupport.findWalletByUserIdForUpdate(payment.getPaymentUser().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_WALLET_NOT_FOUND));
        Wallet systemWallet = paymentSupport.findSystemWalletForUpdate()
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_SYSTEM_WALLET_NOT_FOUND));

        if (systemWallet.getBalance() < requestAmount) {
            log.warn("결제 취소 실패 - 시스템 지갑 잔액 부족. user={}, amount={}", payment.getPaymentUser().getId(), requestAmount);
            throw new BusinessException(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
        }

        systemWallet.debit(requestAmount, CashEventType.환불__주문취소__결제금액, "Order", orderDto.getAuctionOrderId());
        payerWallet.credit(requestAmount, CashEventType.환불__주문취소__결제금액, "Order", orderDto.getAuctionOrderId());

        payment.decreaseBalance(requestAmount);
        Refund refund = Refund.create(payment, requestAmount, cancelReason, null);
        refundRepository.save(refund);
        paymentRepository.save(payment);

        PaymentCanceledEvent event = new PaymentCanceledEvent(orderDto, cancelReason, requestAmount);
        if (eventPublishingConfig.isKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_PAYMENT, payment.getId(),
                    PaymentEventMapper.EventType.PAYMENT_CANCELED.name(),
                    Map.of("eventType", PaymentEventMapper.EventType.PAYMENT_CANCELED.name(),
                            "aggregateId", payment.getId(), "data", event));
        }
        eventPublisher.publish(event);

        return refund;
    }
}
