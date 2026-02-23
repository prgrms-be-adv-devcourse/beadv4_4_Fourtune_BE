package com.fourtune.api.infrastructure.kafka.payment;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 Outbox payload 래퍼
 * UseCase에서 append 시 Map으로 이 구조를 저장하고, Handler에서 payload 문자열만 받아 역직렬화할 때 사용
 * aggregateId는 결제 성공 시 payment.getId()(Long), 실패/재시도 시 orderId(String) 또는 0L 등 혼용되므로 Object로 수용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventPayload {

    private String eventType;
    private Object aggregateId;  // Long(paymentId) 또는 String(orderId) 혼용
    private JsonNode data;
}
