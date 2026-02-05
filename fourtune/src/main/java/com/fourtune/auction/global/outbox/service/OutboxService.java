package com.fourtune.auction.global.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.global.outbox.domain.OutboxEvent;
import com.fourtune.auction.global.outbox.repository.OutboxEventRepository;
import com.fourtune.auction.shared.user.dto.UserResponse;
import com.fourtune.auction.shared.user.kafka.UserEventMessage;
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

    private static final String AGGREGATE_TYPE_USER = "User";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveUserJoinedEvent(UserResponse user) {
        saveUserEvent(user, UserEventMessage.UserEventType.USER_JOINED);
    }

    @Transactional
    public void saveUserModifiedEvent(UserResponse user) {
        saveUserEvent(user, UserEventMessage.UserEventType.USER_MODIFIED);
    }

    @Transactional
    public void saveUserDeletedEvent(UserResponse user) {
        saveUserEvent(user, UserEventMessage.UserEventType.USER_DELETED);
    }

    private void saveUserEvent(UserResponse user, UserEventMessage.UserEventType eventType) {
        try {
            UserEventMessage message = createUserEventMessage(user, eventType);
            String payload = objectMapper.writeValueAsString(message);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(AGGREGATE_TYPE_USER)
                    .aggregateId(user.id())
                    .eventType(eventType.name())
                    .payload(payload)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.debug("Outbox 이벤트 저장 완료: type={}, userId={}", eventType, user.id());

        } catch (JsonProcessingException e) {
            log.error("Outbox 이벤트 직렬화 실패: type={}, userId={}", eventType, user.id(), e);
            throw new RuntimeException("이벤트 직렬화 실패", e);
        }
    }

    private UserEventMessage createUserEventMessage(UserResponse user, UserEventMessage.UserEventType eventType) {
        return switch (eventType) {
            case USER_JOINED -> UserEventMessage.fromUserJoined(user);
            case USER_MODIFIED -> UserEventMessage.fromUserModified(user);
            case USER_DELETED -> UserEventMessage.fromUserDeleted(user);
        };
    }
}
