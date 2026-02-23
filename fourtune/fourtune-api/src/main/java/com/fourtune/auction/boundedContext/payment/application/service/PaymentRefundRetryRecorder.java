package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 환불 PG 취소 API 실패 시 재시도용 이벤트를 Outbox에 기록.
 * REQUIRES_NEW로 별도 트랜잭션에 커밋하여, 메인 트랜잭션 롤백과 무관하게 보존.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundRetryRecorder {

    private static final String AGGREGATE_TYPE_PAYMENT = "Payment";
    private static final String EVENT_TYPE_REFUND_PG_RETRY = "REFUND_PG_RETRY";

    private final OutboxService outboxService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRefundPgRetry(String paymentKey, String orderId, Long amount, String cancelReason) {
        log.warn("환불 PG 재시도 이벤트 기록: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
        outboxService.append(AGGREGATE_TYPE_PAYMENT, 0L, EVENT_TYPE_REFUND_PG_RETRY,
                Map.of("eventType", EVENT_TYPE_REFUND_PG_RETRY, "aggregateId", 0L, "data", Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount, "cancelReason", cancelReason != null ? cancelReason : "")));
    }
}
