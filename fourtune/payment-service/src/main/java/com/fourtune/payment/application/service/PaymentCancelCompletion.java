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
 *
 * [동시성 보호]
 * Payment 엔티티를 비관적 락(SELECT FOR UPDATE)으로 재조회한 후 잔액을 재검증한다.
 * PG 호출과 DB 처리 사이의 Race Condition(Lost Update)을 방지하기 위함이다.
 * PG는 이미 취소됐는데 DB 재검증에서 실패하는 경우(극히 드문 동시 요청)는
 * 운영팀이 PG 취소 이력으로 수동 대사하도록 CRITICAL 로그를 남긴다.
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
    public Refund completeCancelInDb(Payment paymentParam, OrderDto orderDto, Long requestAmount, String cancelReason) {
        // [DEFECT-001 수정] stale 파라미터 대신 비관적 락으로 Payment를 재조회해 Lost Update를 방지한다.
        // 락 획득 순서: Payment → User Wallet → System Wallet (일관된 순서 유지)
        Payment payment = paymentRepository.findByIdForUpdate(paymentParam.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 락 안에서 잔액을 재검증한다. 동시 요청으로 이미 다른 스레드가 차감했을 수 있다.
        if (payment.getBalanceAmount() < requestAmount) {
            log.error("CRITICAL: 락 획득 후 잔액 부족 감지 (동시 부분취소 요청 의심) — PG는 이미 취소됨, 수동 대사 필요. " +
                            "paymentId={}, currentBalance={}, requestedAmount={}",
                    payment.getId(), payment.getBalanceAmount(), requestAmount);
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_AMOUNT_EXCEEDS_BALANCE);
        }

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
