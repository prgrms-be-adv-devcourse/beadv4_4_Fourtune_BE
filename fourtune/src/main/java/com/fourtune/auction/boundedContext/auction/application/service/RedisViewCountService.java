package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 조회수 Redis 캐시 서비스
 * - INCR로 조회수 증가 (DB 부하 감소)
 * - 1분마다 Redis → DB 동기화: SCAN으로 키 수집, MGET 1번으로 값 조회, DB 반영 후 커밋 성공 시 Pipeline으로 DECRBY 일괄 전송
 *   (DB 롤백 시 유실 방지; SCAN으로 KEYS 블로킹 방지; MGET+Pipeline으로 Redis 왕복 최소화)
 * - 상세/목록 응답 시 DB + Redis 합산 값 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisViewCountService {

    private final StringRedisTemplate redisTemplate;
    private final AuctionItemRepository auctionItemRepository;

    @Value("${app.view-count.key-prefix:auction:view:}")
    private String keyPrefix;

    @Value("${app.view-count.use-redis:true}")
    private boolean useRedis;

    /**
     * 조회수 1 증가 (Redis INCR). use-redis=false면 no-op
     */
    public void incrementViewCount(Long auctionId) {
        if (!useRedis || auctionId == null) return;
        String key = keyPrefix + auctionId;
        Long v = redisTemplate.opsForValue().increment(key);
        log.trace("[VIEW] increment auctionId={} count={}", auctionId, v);
    }

    /**
     * 단일 경매 조회수 = DB viewCount + Redis 캐시 값 (상세 응답용). use-redis=false면 dbViewCount 그대로 반환
     */
    public long getViewCount(Long auctionId, long dbViewCount) {
        if (!useRedis || auctionId == null) return dbViewCount;
        String key = keyPrefix + auctionId;
        String cached = redisTemplate.opsForValue().get(key);
        long redisDelta = (cached != null) ? Long.parseLong(cached) : 0L;
        return dbViewCount + redisDelta;
    }

    /**
     * 여러 경매 조회수 = DB viewCount + Redis 캐시 값 (목록 응답용, mget). use-redis=false면 dbViewCountByAuctionId 그대로 반환
     */
    public Map<Long, Long> getViewCounts(Collection<Long> auctionIds, Map<Long, Long> dbViewCountByAuctionId) {
        if (!useRedis || auctionIds == null || auctionIds.isEmpty()) return dbViewCountByAuctionId;
        List<String> keys = auctionIds.stream().map(id -> keyPrefix + id).toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null) return dbViewCountByAuctionId;

        Map<Long, Long> result = new HashMap<>(dbViewCountByAuctionId);
        int i = 0;
        for (Long id : auctionIds) {
            long dbVal = result.getOrDefault(id, 0L);
            String v = (i < values.size()) ? values.get(i) : null;
            long redisDelta = (v != null && !v.isEmpty()) ? Long.parseLong(v) : 0L;
            result.put(id, dbVal + redisDelta);
            i++;
        }
        return result;
    }

    /**
     * Redis 값 → DB 반영 후, DB 커밋 성공 시에만 Redis에서 해당 delta만큼 DECRBY.
     * SCAN으로 키 수집(KEYS 블로킹 방지), MGET 1번으로 값 조회, afterCommit에서 Pipeline으로 DECRBY 일괄 전송.
     */
    @Transactional
    @Scheduled(fixedDelayString = "${app.view-count.sync-interval-ms:60000}")
    @SuppressWarnings("deprecation") // connection.scan(ScanOptions) 대체 API 전까지 SCAN 사용(블로킹 방지)
    public void syncToDatabase() {
        if (!useRedis) return;

        Set<String> keySet = redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> result = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match(keyPrefix + "*").count(100).build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    result.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return result;
        });
        if (keySet == null || keySet.isEmpty()) return;

        List<String> keyList = new ArrayList<>(keySet);
        List<String> values = redisTemplate.opsForValue().multiGet(keyList);
        if (values == null) return;

        List<String> keysToAdjust = new ArrayList<>();
        List<Long> auctionIds = new ArrayList<>();
        List<Long> deltas = new ArrayList<>();

        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
            String val = (i < values.size()) ? values.get(i) : null;
            if (val == null || val.isEmpty()) continue;
            long delta;
            try {
                delta = Long.parseLong(val);
            } catch (NumberFormatException e) {
                log.warn("[VIEW] invalid value key={} val={}", key, val);
                continue;
            }
            if (delta <= 0) continue;
            String suffix = key.substring(keyPrefix.length());
            try {
                long auctionId = Long.parseLong(suffix);
                keysToAdjust.add(key);
                auctionIds.add(auctionId);
                deltas.add(delta);
            } catch (NumberFormatException e) {
                log.warn("[VIEW] invalid key suffix key={}", key);
            }
        }

        if (auctionIds.isEmpty()) return;

        for (int i = 0; i < auctionIds.size(); i++) {
            auctionItemRepository.addViewCount(auctionIds.get(i), deltas.get(i));
        }

        List<String> keysForCallback = new ArrayList<>(keysToAdjust);
        List<Long> deltasForCallback = new ArrayList<>(deltas);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisTemplate.executePipelined(new SessionCallback<Object>() {
                    @Override
                    @SuppressWarnings({"rawtypes", "unchecked"})
                    public Object execute(org.springframework.data.redis.core.RedisOperations operations) {
                        for (int i = 0; i < keysForCallback.size(); i++) {
                            operations.opsForValue().decrement(keysForCallback.get(i), deltasForCallback.get(i));
                        }
                        return null;
                    }
                });
            }
        });

        log.info("[VIEW] synced to DB auctionIds={} count={}", auctionIds.size(), auctionIds);
    }
}
