package com.fourtune.api.infrastructure.kafka.settlement;

import com.fourtune.shared.settlement.event.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka Consumer 역직렬화용: eventType 문자열 → 이벤트 Class 매핑
 */
@Slf4j
public final class SettlementEventMapper {

    private static final Map<String, Class<?>> TYPE_TO_CLASS = new ConcurrentHashMap<>();

    static {
        TYPE_TO_CLASS.put(EventType.SETTLEMENT_COMPLETED.name(), SettlementCompletedEvent.class);
        TYPE_TO_CLASS.put(EventType.SETTLEMENT_USER_CREATED.name(), SettlementUserCreatedEvent.class);
    }

    private SettlementEventMapper() {
    }

    /**
     * eventType 문자열에 해당하는 이벤트 Class 반환 (Consumer 역직렬화 시 사용)
     */
    public static Class<?> getClass(String eventType) {
        Class<?> clazz = TYPE_TO_CLASS.get(eventType);
        if (clazz == null) {
            log.warn("알 수 없는 정산 이벤트 타입: {}", eventType);
            throw new IllegalArgumentException("Unknown settlement event type: " + eventType);
        }
        return clazz;
    }

    /**
     * 정산 이벤트 타입 정의
     */
    public enum EventType {
        SETTLEMENT_COMPLETED,
        SETTLEMENT_USER_CREATED
    }
}