package com.fourtune.auction.global.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.global.config.EventPublishingConfig;
import com.fourtune.auction.global.outbox.domain.OutboxEvent;
import com.fourtune.auction.global.outbox.domain.OutboxEventStatus;
import com.fourtune.auction.global.outbox.repository.OutboxEventRepository;
import com.fourtune.auction.shared.user.kafka.UserEventMessage;
import com.fourtune.auction.shared.user.kafka.UserKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 이벤트 발행자
 * 주기적으로 Outbox 테이블을 폴링하여 Kafka로 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final UserKafkaProducer userKafkaProducer;
    private final ObjectMapper objectMapper;
    private final EventPublishingConfig eventPublishingConfig;

    @Value("${outbox.publisher.batch-size:100}")
    private int batchSize;

    @Value("${outbox.publisher.max-retry-count:3}")
    private int maxRetryCount;

    /**
     * 대기 중인 이벤트 발행 (1초마다 실행)
     */
    @Scheduled(fixedDelayString = "${outbox.publisher.poll-interval-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        if (!eventPublishingConfig.isUserEventsKafkaEnabled()) {
            return;
        }

        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents(
                OutboxEventStatus.PENDING, batchSize);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Outbox 발행 시작: {} 건", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                publishEvent(event);
                event.markAsPublished();
                log.debug("Outbox 이벤트 발행 성공: id={}, type={}", event.getId(), event.getEventType());
            } catch (Exception e) {
                event.markAsFailed();
                log.error("Outbox 이벤트 발행 실패: id={}, type={}, error={}",
                        event.getId(), event.getEventType(), e.getMessage(), e);
            }
        }

        outboxEventRepository.saveAll(pendingEvents);
    }

    /**
     * 실패한 이벤트 재시도 (30초마다 실행)
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void retryFailedEvents() {
        if (!eventPublishingConfig.isUserEventsKafkaEnabled()) {
            return;
        }

        List<OutboxEvent> failedEvents = outboxEventRepository.findRetryableEvents(
                OutboxEventStatus.FAILED, maxRetryCount);

        if (failedEvents.isEmpty()) {
            return;
        }

        log.info("Outbox 재시도 시작: {} 건", failedEvents.size());

        for (OutboxEvent event : failedEvents) {
            event.retry();
        }

        outboxEventRepository.saveAll(failedEvents);
    }

    /**
     * 오래된 발행 완료 이벤트 정리 (매일 새벽 3시 실행)
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        int deleted = outboxEventRepository.deleteOldPublishedEvents(OutboxEventStatus.PUBLISHED, threshold);
        log.info("Outbox 정리 완료: {} 건 삭제", deleted);
    }

    private void publishEvent(OutboxEvent event) throws Exception {
        if ("User".equals(event.getAggregateType())) {
            UserEventMessage message = objectMapper.readValue(event.getPayload(), UserEventMessage.class);
            userKafkaProducer.publishSync(message);
        } else {
            log.warn("알 수 없는 aggregate type: {}", event.getAggregateType());
        }
    }
}
