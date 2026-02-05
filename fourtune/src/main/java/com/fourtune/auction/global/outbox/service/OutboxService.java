package com.fourtune.auction.global.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.global.outbox.domain.OutboxEvent;
import com.fourtune.auction.global.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox 이벤트 저장 서비스
 * 트랜잭션 내에서 이벤트를 Outbox 테이블에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void append(String aggregateType, Long aggregateId, String eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.debug("Outbox 이벤트 저장 완료: aggregateType={}, aggregateId={}, eventType={}",
                    aggregateType, aggregateId, eventType);

        } catch (JsonProcessingException e) {
            log.error("Outbox 이벤트 직렬화 실패: aggregateType={}, aggregateId={}, eventType={}",
                    aggregateType, aggregateId, eventType, e);
            throw new RuntimeException("이벤트 직렬화 실패", e);
        }
    }
}
