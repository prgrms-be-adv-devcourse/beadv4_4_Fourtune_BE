package com.fourtune.payment.application.service;

import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.payment.domain.constant.PaymentStatus;
import com.fourtune.payment.domain.entity.Payment;
import com.fourtune.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.payment.port.out.PaymentGatewayPort;
import com.fourtune.payment.port.out.PaymentRepository;
import com.fourtune.shared.kafka.payment.PaymentEventMapper;
import com.fourtune.shared.payment.dto.OrderDto;
import com.fourtune.shared.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmUseCase {

    private static final String AGGREGATE_TYPE_PAYMENT = "Payment";

    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentConfirmInternalUseCase paymentConfirmInternalUseCase;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;
    private final OutboxService outboxService;
    private final EventPublishingConfig eventPublishingConfig;

    public PaymentExecutionResult confirmPayment(String paymentKey, String orderId, Long pgAmount, Long userId) {
        var existing = paymentRepository.findPaymentByOrderId(orderId);
        if (existing.isPresent()) {
            Payment p = existing.get();
            if (p.getStatus() == PaymentStatus.APPROVED) {
                log.debug("이미 처리된 결제 orderId={}, 기존 결과 반환", orderId);
                return PaymentExecutionResult.success(p.getPaymentKey(), p.getOrderId(), p.getAmount());
            }
        }

        PaymentExecutionResult result = paymentGatewayPort.confirm(paymentKey, orderId, pgAmount);

        try {
            paymentConfirmInternalUseCase.processInternalSystemLogic(orderId, pgAmount, paymentKey, userId);
        } catch (Exception e) {
            log.error("내부 시스템 처리 실패. 결제 승인 취소를 진행합니다. orderId={}, error={}", orderId, e.getMessage());

            try {
                paymentGatewayPort.cancel(paymentKey, "System Logic Failed: " + e.getMessage(), null);
            } catch (Exception cancelEx) {
                log.error("CRITICAL: 결제 취소 실패! (수동 환불 필요) paymentKey={}, error={}", paymentKey, cancelEx.getMessage());
                publishPaymentFailed(orderId, userId, "P311", "결제 취소 실패(관리자 문의)", pgAmount, 0L);
                throw new BusinessException(ErrorCode.PAYMENT_PG_REFUND_FAILED);
            }

            publishPaymentFailed(orderId, userId, "500", "내부 시스템 오류로 결제가 취소되었습니다.", pgAmount, 0L);
            throw e;
        }
        return result;
    }

    private void publishPaymentFailed(String orderId, Long userId, String code, String message, Long pgAmount, Long shortfall) {
        OrderDto orderDto = OrderDto.builder().orderId(orderId).userId(userId).items(null).build();
        PaymentFailedEvent event = new PaymentFailedEvent(code, message, orderDto, pgAmount, shortfall);
        if (eventPublishingConfig.isKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_PAYMENT, 0L, PaymentEventMapper.EventType.PAYMENT_FAILED.name(),
                    Map.of("eventType", PaymentEventMapper.EventType.PAYMENT_FAILED.name(), "aggregateId", orderId, "data", event));
        }
        eventPublisher.publish(event);
    }
}
