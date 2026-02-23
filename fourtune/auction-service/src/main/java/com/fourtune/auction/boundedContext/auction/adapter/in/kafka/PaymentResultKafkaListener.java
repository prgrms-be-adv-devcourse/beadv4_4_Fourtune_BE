package com.fourtune.auction.boundedContext.auction.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.auction.application.service.OrderCompleteUseCase;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.shared.payment.event.PaymentFailedEvent;
import com.fourtune.shared.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * payment-events 토픽 구독 — 결제 완료/실패 시 주문 상태 반영 (MSA 분리 시 Payment → Auction 연동)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentResultKafkaListener {

    private static final String HEADER_EVENT_TYPE = "X-Event-Type";

    private final ObjectMapper objectMapper;
    private final OrderCompleteUseCase orderCompleteUseCase;

    @KafkaListener(
            topics = KafkaTopicConfig.PAYMENT_EVENTS_TOPIC,
            groupId = "auction-payment-events-group",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(
            @Payload String payload,
            @Header(HEADER_EVENT_TYPE) String eventType,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        if (!"PAYMENT_SUCCEEDED".equals(eventType) && !"PAYMENT_FAILED".equals(eventType)) {
            log.debug("Auction에서 무시하는 결제 이벤트: eventType={}", eventType);
            return;
        }

        log.info("Payment 결과 수신(주문 반영): eventType={}, key={}", eventType, key);

        try {
            if ("PAYMENT_SUCCEEDED".equals(eventType)) {
                PaymentSucceededEvent event = objectMapper.readValue(payload, PaymentSucceededEvent.class);
                if (event.getOrder() != null && event.getOrder().getOrderId() != null) {
                    orderCompleteUseCase.completePayment(event.getOrder().getOrderId(), "");
                    log.info("주문 완료 처리 완료: orderId={}", event.getOrder().getOrderId());
                } else {
                    log.warn("PAYMENT_SUCCEEDED 수신했으나 order 정보 없음: key={}", key);
                }
            } else {
                PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);
                if (event.getOrder() != null && event.getOrder().getOrderId() != null) {
                    String reason = event.getMsg() != null ? event.getMsg() : "결제 실패";
                    orderCompleteUseCase.failPayment(event.getOrder().getOrderId(), reason);
                    log.info("주문 실패 처리 완료: orderId={}, reason={}", event.getOrder().getOrderId(), reason);
                } else {
                    log.warn("PAYMENT_FAILED 수신했으나 order 정보 없음: key={}", key);
                }
            }
        } catch (Exception e) {
            log.error("Payment 결과 처리 실패: eventType={}, key={}, error={}", eventType, key, e.getMessage(), e);
            throw new RuntimeException("Payment 결과 처리 실패", e);
        }
    }
}
