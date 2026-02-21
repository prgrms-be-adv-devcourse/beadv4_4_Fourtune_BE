package com.fourtune.shared.kafka.payment;

import com.fourtune.shared.payment.event.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka Consumer 역직렬화용: eventType 문자열 → 이벤트 Class 매핑
 */
@Slf4j
public final class PaymentEventMapper {

    private static final Map<String, Class<?>> TYPE_TO_CLASS = new ConcurrentHashMap<>();

    static {
        TYPE_TO_CLASS.put(EventType.PAYMENT_SUCCEEDED.name(), PaymentSucceededEvent.class);
        TYPE_TO_CLASS.put(EventType.PAYMENT_FAILED.name(), PaymentFailedEvent.class);
        TYPE_TO_CLASS.put(EventType.PAYMENT_USER_CREATED.name(), PaymentUserCreatedEvent.class);
        TYPE_TO_CLASS.put(EventType.AUCTION_REFUND_COMPLETED.name(), AuctionRefundCompletedEvent.class);
    }

    private PaymentEventMapper() {
    }

    /**
     * eventType 문자열에 해당하는 이벤트 Class 반환 (Consumer 역직렬화 시 사용)
     */
    public static Class<?> getClass(String eventType) {
        Class<?> clazz = TYPE_TO_CLASS.get(eventType);
        if (clazz == null) {
            log.warn("알 수 없는 결제 이벤트 타입: {}", eventType);
            throw new IllegalArgumentException("Unknown payment event type: " + eventType);
        }
        return clazz;
    }

    /**
     * 결제 이벤트 타입 정의
     */
    public enum EventType {
        PAYMENT_SUCCEEDED,
        PAYMENT_FAILED,
        PAYMENT_USER_CREATED,
        AUCTION_REFUND_COMPLETED
    }
}