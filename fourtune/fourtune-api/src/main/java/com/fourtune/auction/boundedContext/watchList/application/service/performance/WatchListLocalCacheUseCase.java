package com.fourtune.auction.boundedContext.watchList.application.service.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.common.global.config.EventPublishingConfig;
import com.fourtune.common.global.eventPublisher.EventPublisher;
import com.fourtune.common.shared.watchList.event.WatchListAuctionStartedEvent;
import com.fourtune.common.shared.watchList.event.WatchListAuctionEndedEvent;
import com.fourtune.common.shared.watchList.kafka.WatchListEventType;
import com.fourtune.common.shared.watchList.kafka.WatchListKafkaProducer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local Cache (Caffeine) 방식 WatchList 서비스
 *
 * 특징:
 * - 조회: O(1) 메모리 접근 (네트워크 레이턴시 없음)
 * - 업데이트: O(1) 메모리 접근
 * - DB/Redis 접근 없음, 가장 빠름
 *
 * 단점:
 * - OOM 위험 (대량 데이터 시)
 * - 멀티 인스턴스 환경에서 동기화 문제
 * - 서버 재시작 시 캐시 손실
 *
 * 캐시 구조:
 * - auctionToUsersCache: auctionItemId -> Set<userId>
 * - startAlertSentCache: auctionItemId -> Set<userId> (시작 알림 발송 완료)
 * - endAlertSentCache: auctionItemId -> Set<userId> (종료 알림 발송 완료)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WatchListLocalCacheUseCase {

    private final EventPublisher eventPublisher;
    private final EventPublishingConfig eventPublishingConfig;
    private final ObjectMapper objectMapper;
    private final WatchListKafkaProducer watchListKafkaProducer;

    // 관심등록 캐시: auctionItemId -> Set<userId>
    private Cache<Long, Set<Long>> auctionToUsersCache;

    // 알림 발송 완료 캐시
    private Cache<Long, Set<Long>> startAlertSentCache;
    private Cache<Long, Set<Long>> endAlertSentCache;

    // OOM 테스트용: 제한 없는 캐시
    private Cache<Long, Set<Long>> unlimitedCache;

    @PostConstruct
    public void init() {
        // 일반 캐시 (최대 10만 항목, 실제 운영 시 적절한 제한 필요)
        this.auctionToUsersCache = Caffeine.newBuilder()
                .maximumSize(100_000)
                .recordStats()
                .build();

        this.startAlertSentCache = Caffeine.newBuilder()
                .maximumSize(100_000)
                .build();

        this.endAlertSentCache = Caffeine.newBuilder()
                .maximumSize(100_000)
                .build();

        // OOM 테스트용: 제한 없는 캐시 (위험!)
        this.unlimitedCache = Caffeine.newBuilder()
                .recordStats()
                .build();
    }

    /**
     * 관심상품 등록 (Local Cache에 추가)
     */
    public void addInterest(Long userId, Long auctionItemId) {
        auctionToUsersCache.asMap()
                .computeIfAbsent(auctionItemId, k -> ConcurrentHashMap.newKeySet())
                .add(userId);
        log.debug("[LOCAL] 관심등록: user={}, auction={}", userId, auctionItemId);
    }

    /**
     * 관심상품 해제 (Local Cache에서 제거)
     */
    public void removeInterest(Long userId, Long auctionItemId) {
        Set<Long> users = auctionToUsersCache.getIfPresent(auctionItemId);
        if (users != null) {
            users.remove(userId);
        }
        log.debug("[LOCAL] 관심해제: user={}, auction={}", userId, auctionItemId);
    }

    /**
     * 경매의 관심등록 유저 조회
     */
    public Set<Long> getInterestedUsers(Long auctionItemId) {
        Set<Long> users = auctionToUsersCache.getIfPresent(auctionItemId);
        return users != null ? users : Collections.emptySet();
    }

    /**
     * 경매 시작 알림 처리 (Local Cache 방식)
     * DB/Redis 접근 없음, 메모리 연산만 수행
     */
    public WatchListBulkUseCase.ProcessResult processAuctionStart(Long auctionItemId) {
        long startTime = System.currentTimeMillis();

        Set<Long> userIds = getInterestedUsers(auctionItemId);

        if (userIds.isEmpty()) {
            return new WatchListBulkUseCase.ProcessResult(0, 0, 0);
        }

        // 이벤트 발행
        publishWatchListEvent(userIds.stream().toList(), auctionItemId, WatchListEventType.WATCHLIST_AUCTION_STARTED);

        // 알림 발송 완료 마킹 (메모리)
        startAlertSentCache.asMap()
                .computeIfAbsent(auctionItemId, k -> ConcurrentHashMap.newKeySet())
                .addAll(userIds);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[LOCAL] 경매 {} 시작 알림 처리 완료: {}명, {}ms", auctionItemId, userIds.size(), duration);

        return new WatchListBulkUseCase.ProcessResult(userIds.size(), 0, duration);
    }

    /**
     * 경매 종료 알림 처리 (Local Cache 방식)
     */
    public WatchListBulkUseCase.ProcessResult processAuctionEnd(Long auctionItemId) {
        long startTime = System.currentTimeMillis();

        Set<Long> userIds = getInterestedUsers(auctionItemId);

        if (userIds.isEmpty()) {
            return new WatchListBulkUseCase.ProcessResult(0, 0, 0);
        }

        publishWatchListEvent(userIds.stream().toList(), auctionItemId, WatchListEventType.WATCHLIST_AUCTION_ENDED);

        endAlertSentCache.asMap()
                .computeIfAbsent(auctionItemId, k -> ConcurrentHashMap.newKeySet())
                .addAll(userIds);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[LOCAL] 경매 {} 종료 알림 처리 완료: {}명, {}ms", auctionItemId, userIds.size(), duration);

        return new WatchListBulkUseCase.ProcessResult(userIds.size(), 0, duration);
    }

    private void publishWatchListEvent(List<Long> users, Long auctionItemId, WatchListEventType type) {
        if (eventPublishingConfig.isWatchlistEventsKafkaEnabled()) {
            try {
                String payload = objectMapper.writeValueAsString(Map.of("users", users, "auctionItemId", auctionItemId));
                watchListKafkaProducer.send(String.valueOf(auctionItemId), payload, type.name());
            } catch (Exception e) {
                log.error("[LOCAL] WatchList Kafka 이벤트 발행 실패: auctionItemId={}", auctionItemId, e);
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
     * 테스트용: 캐시 초기화
     */
    public void clearCache() {
        auctionToUsersCache.invalidateAll();
        startAlertSentCache.invalidateAll();
        endAlertSentCache.invalidateAll();
        unlimitedCache.invalidateAll();
    }

    /**
     * 테스트용: 대량 관심등록 (벤치마크용)
     */
    public void bulkAddInterest(Long auctionItemId, List<Long> userIds) {
        auctionToUsersCache.asMap()
                .computeIfAbsent(auctionItemId, k -> ConcurrentHashMap.newKeySet())
                .addAll(userIds);
    }

    // ========== OOM 테스트용 메서드 ==========

    /**
     * OOM 테스트: 제한 없는 캐시에 데이터 추가
     */
    public void addToUnlimitedCache(Long auctionItemId, List<Long> userIds) {
        unlimitedCache.asMap()
                .computeIfAbsent(auctionItemId, k -> ConcurrentHashMap.newKeySet())
                .addAll(userIds);
    }

    /**
     * 메모리 사용량 통계 조회
     */
    public MemoryStats getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        return new MemoryStats(
                runtime.totalMemory(),
                runtime.freeMemory(),
                runtime.maxMemory(),
                auctionToUsersCache.estimatedSize(),
                unlimitedCache.estimatedSize()
        );
    }

    /**
     * 메모리 통계 DTO
     */
    public record MemoryStats(
            long totalMemory,
            long freeMemory,
            long maxMemory,
            long limitedCacheSize,
            long unlimitedCacheSize
    ) {
        public long usedMemory() {
            return totalMemory - freeMemory;
        }

        public long usedMemoryMb() {
            return usedMemory() / 1024 / 1024;
        }

        public long maxMemoryMb() {
            return maxMemory / 1024 / 1024;
        }

        public double usageRatio() {
            return (double) usedMemory() / maxMemory;
        }

        public String toSummary() {
            return String.format("Memory: %dMB / %dMB (%.1f%%), LimitedCache: %d, UnlimitedCache: %d",
                    usedMemoryMb(), maxMemoryMb(), usageRatio() * 100,
                    limitedCacheSize, unlimitedCacheSize);
        }
    }
}
