package com.fourtune.payment.application.service;

import com.fourtune.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
                Map.of("eventType", EVENT_TYPE_REFUND_PG_RETRY, "paymentKey", paymentKey, "orderId", orderId, "amount", amount, "cancelReason", cancelReason != null ? cancelReason : ""));
    }
}
