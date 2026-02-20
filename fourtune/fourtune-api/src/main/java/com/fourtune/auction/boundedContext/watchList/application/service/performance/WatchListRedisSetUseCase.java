package com.fourtune.auction.boundedContext.watchList.application.service.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.watchList.event.WatchListAuctionStartedEvent;
import com.fourtune.shared.watchList.event.WatchListAuctionEndedEvent;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListEventType;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis Set 방식 WatchList 서비스
 *
 * 특징:
 * - 조회: O(N) SMEMBERS (Redis, DB 접근 없음)
 * - 업데이트: O(N) SADD (Redis, DB 접근 없음)
 * - DB 쿼리 0회, 모든 처리가 Redis에서 완료
 *
 * Redis Key 구조:
 * - watchlist:auction:{auctionItemId} -> Set<userId> (관심등록 유저)
 * - watchlist:alert:start:{auctionItemId} -> Set<userId> (시작 알림 발송 완료)
 * - watchlist:alert:end:{auctionItemId} -> Set<userId> (종료 알림 발송 완료)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WatchListRedisSetUseCase {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EventPublisher eventPublisher;
    private final EventPublishingConfig eventPublishingConfig;
    private final ObjectMapper objectMapper;
    private final WatchListKafkaProducer watchListKafkaProducer;

    private static final String AUCTION_USERS_KEY = "watchlist:auction:";
    private static final String ALERT_START_SENT_KEY = "watchlist:alert:start:";
    private static final String ALERT_END_SENT_KEY = "watchlist:alert:end:";

    /**
     * 관심상품 등록 (Redis Set에 추가)
     */
    public void addInterest(Long userId, Long auctionItemId) {
        String key = AUCTION_USERS_KEY + auctionItemId;
        redisTemplate.opsForSet().add(key, userId);
        log.debug("[REDIS] 관심등록: user={}, auction={}", userId, auctionItemId);
    }

    /**
     * 관심상품 해제 (Redis Set에서 제거)
     */
    public void removeInterest(Long userId, Long auctionItemId) {
        String key = AUCTION_USERS_KEY + auctionItemId;
        redisTemplate.opsForSet().remove(key, userId);
        log.debug("[REDIS] 관심해제: user={}, auction={}", userId, auctionItemId);
    }

    /**
     * 경매의 관심등록 유저 조회
     */
    public Set<Long> getInterestedUsers(Long auctionItemId) {
        String key = AUCTION_USERS_KEY + auctionItemId;
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream()
                .map(o -> ((Number) o).longValue())
                .collect(Collectors.toSet());
    }

    /**
     * 경매 시작 알림 처리 (Redis Set 방식)
     * DB 쿼리 0회, Redis 연산만 수행
     */
    public WatchListBulkUseCase.ProcessResult processAuctionStart(Long auctionItemId) {
        long startTime = System.currentTimeMillis();

        // Redis SMEMBERS: 관심등록 유저 조회
        Set<Long> userIds = getInterestedUsers(auctionItemId);

        if (userIds.isEmpty()) {
            return new WatchListBulkUseCase.ProcessResult(0, 0, 0);
        }

        // 이벤트 발행
        publishWatchListEvent(userIds.stream().toList(), auctionItemId, WatchListEventType.WATCHLIST_AUCTION_STARTED);

        // Redis SADD: 알림 발송 완료 마킹
        markStartAlertSent(auctionItemId, userIds);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[REDIS] 경매 {} 시작 알림 처리 완료: {}명, {}ms", auctionItemId, userIds.size(), duration);

        return new WatchListBulkUseCase.ProcessResult(userIds.size(), 0, duration);
    }

    /**
     * 경매 종료 알림 처리 (Redis Set 방식)
     */
    public WatchListBulkUseCase.ProcessResult processAuctionEnd(Long auctionItemId) {
        long startTime = System.currentTimeMillis();

        Set<Long> userIds = getInterestedUsers(auctionItemId);

        if (userIds.isEmpty()) {
            return new WatchListBulkUseCase.ProcessResult(0, 0, 0);
        }

        publishWatchListEvent(userIds.stream().toList(), auctionItemId, WatchListEventType.WATCHLIST_AUCTION_ENDED);
        markEndAlertSent(auctionItemId, userIds);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[REDIS] 경매 {} 종료 알림 처리 완료: {}명, {}ms", auctionItemId, userIds.size(), duration);

        return new WatchListBulkUseCase.ProcessResult(userIds.size(), 0, duration);
    }

    /**
     * 시작 알림 발송 완료 마킹
     */
    private void markStartAlertSent(Long auctionItemId, Set<Long> userIds) {
        String key = ALERT_START_SENT_KEY + auctionItemId;
        redisTemplate.opsForSet().add(key, userIds.toArray());
    }

    /**
     * 종료 알림 발송 완료 마킹
     */
    private void markEndAlertSent(Long auctionItemId, Set<Long> userIds) {
        String key = ALERT_END_SENT_KEY + auctionItemId;
        redisTemplate.opsForSet().add(key, userIds.toArray());
    }

    private void publishWatchListEvent(List<Long> users, Long auctionItemId, WatchListEventType type) {
        if (eventPublishingConfig.isWatchlistEventsKafkaEnabled()) {
            try {
                String payload = objectMapper.writeValueAsString(Map.of("users", users, "auctionItemId", auctionItemId));
                watchListKafkaProducer.send(String.valueOf(auctionItemId), payload, type.name());
            } catch (Exception e) {
                log.error("[REDIS] WatchList Kafka 이벤트 발행 실패: auctionItemId={}", auctionItemId, e);
            }
        } else {
            if (type == WatchListEventType.WATCHLIST_AUCTION_STARTED) {
                eventPublisher.publish(new WatchListAuctionStartedEvent(users, auctionItemId));
            } else {
                eventPublisher.publish(new WatchListAuctionEndedEvent(users, auctionItemId));
            }
        }
    }

    /**
     * 테스트용: 특정 경매의 Redis 데이터 초기화
     */
    public void clearAuctionData(Long auctionItemId) {
        redisTemplate.delete(AUCTION_USERS_KEY + auctionItemId);
        redisTemplate.delete(ALERT_START_SENT_KEY + auctionItemId);
        redisTemplate.delete(ALERT_END_SENT_KEY + auctionItemId);
    }

    /**
     * 테스트용: 대량 관심등록 (벤치마크용)
     */
    public void bulkAddInterest(Long auctionItemId, List<Long> userIds) {
        String key = AUCTION_USERS_KEY + auctionItemId;
        redisTemplate.opsForSet().add(key, userIds.toArray());
    }
}
