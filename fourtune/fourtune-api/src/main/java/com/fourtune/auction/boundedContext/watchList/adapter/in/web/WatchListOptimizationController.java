package com.fourtune.auction.boundedContext.watchList.adapter.in.web;

import com.fourtune.auction.boundedContext.watchList.application.service.performance.WatchListBulkUseCase;
import com.fourtune.auction.boundedContext.watchList.application.service.performance.WatchListLocalCacheUseCase;
import com.fourtune.auction.boundedContext.watchList.application.service.performance.WatchListRedisSetUseCase;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

/**
 * WatchList 최적화 방식 3종 비교 테스트 컨트롤러
 *
 * 테스트 방식:
 * 1. DB Bulk - 1회 조회 + 1회 Bulk UPDATE
 * 2. Redis Set - SMEMBERS + SADD (DB 접근 없음)
 * 3. Local Cache - 메모리 조회/업데이트 (네트워크 없음)
 *
 * 추가 테스트:
 * - OOM 발생 지점 확인 (Local Cache)
 */
@RestController
@RequestMapping("/api/test/watchlist/optimization")
@RequiredArgsConstructor
@Slf4j
@Profile({"local", "dev"})
@Tag(name = "WatchList Optimization Test", description = "관심상품 알림 최적화 방식 비교 테스트 API")
public class WatchListOptimizationController {

    private final WatchListBulkUseCase bulkService;
    private final WatchListRedisSetUseCase redisService;
    private final WatchListLocalCacheUseCase localCacheService;
    private final WatchListRepository watchListRepository;
    private final MeterRegistry meterRegistry;

    private Timer bulkTimer;
    private Timer redisTimer;
    private Timer localCacheTimer;

    @PostConstruct
    public void initMetrics() {
        bulkTimer = Timer.builder("watchlist.optimization.bulk.duration")
                .description("DB Bulk 방식 응답 시간")
                .tag("strategy", "bulk")
                .register(meterRegistry);

        redisTimer = Timer.builder("watchlist.optimization.redis.duration")
                .description("Redis Set 방식 응답 시간")
                .tag("strategy", "redis")
                .register(meterRegistry);

        localCacheTimer = Timer.builder("watchlist.optimization.localcache.duration")
                .description("Local Cache 방식 응답 시간")
                .tag("strategy", "local_cache")
                .register(meterRegistry);
    }

    // ========== 데이터 준비 ==========

    @PostMapping("/setup/{auctionItemId}")
    @Operation(summary = "테스트 데이터 준비", description = "지정된 유저 수만큼 Redis/LocalCache에 관심등록 데이터 생성")
    public ResponseEntity<Map<String, Object>> setupTestData(
            @PathVariable Long auctionItemId,
            @RequestParam(defaultValue = "1000") int userCount) {

        List<Long> userIds = LongStream.rangeClosed(1, userCount).boxed().toList();

        // Redis에 데이터 추가
        redisService.clearAuctionData(auctionItemId);
        redisService.bulkAddInterest(auctionItemId, userIds);

        // Local Cache에 데이터 추가
        localCacheService.clearCache();
        localCacheService.bulkAddInterest(auctionItemId, userIds);

        log.info("테스트 데이터 준비 완료: auctionItemId={}, userCount={}", auctionItemId, userCount);

        return ResponseEntity.ok(Map.of(
                "auctionItemId", auctionItemId,
                "userCount", userCount,
                "message", "Redis와 Local Cache에 테스트 데이터가 준비되었습니다.",
                "note", "DB Bulk 테스트는 실제 DB에 watch_list 데이터가 있어야 합니다."
        ));
    }

    // ========== 성능 비교 테스트 ==========

    @GetMapping("/bulk/{auctionItemId}")
    @Operation(summary = "DB Bulk 방식 테스트", description = "1회 조회 + 1회 Bulk UPDATE로 처리")
    public ResponseEntity<Map<String, Object>> testBulk(@PathVariable Long auctionItemId) {
        return bulkTimer.record(() -> {
            WatchListBulkUseCase.ProcessResult result = bulkService.processAuctionStart(auctionItemId, "");
            return ResponseEntity.ok(Map.of(
                    "strategy", "DB Bulk",
                    "auctionItemId", auctionItemId,
                    "userCount", result.userCount(),
                    "queryCount", result.queryCount(),
                    "durationMs", result.durationMs()
            ));
        });
    }

    @GetMapping("/redis/{auctionItemId}")
    @Operation(summary = "Redis Set 방식 테스트", description = "SMEMBERS + SADD로 처리 (DB 접근 없음)")
    public ResponseEntity<Map<String, Object>> testRedis(@PathVariable Long auctionItemId) {
        return redisTimer.record(() -> {
            WatchListBulkUseCase.ProcessResult result = redisService.processAuctionStart(auctionItemId, "");
            return ResponseEntity.ok(Map.of(
                    "strategy", "Redis Set",
                    "auctionItemId", auctionItemId,
                    "userCount", result.userCount(),
                    "queryCount", result.queryCount(),
                    "durationMs", result.durationMs()
            ));
        });
    }

    @GetMapping("/local-cache/{auctionItemId}")
    @Operation(summary = "Local Cache 방식 테스트", description = "메모리 조회/업데이트 (네트워크 없음)")
    public ResponseEntity<Map<String, Object>> testLocalCache(@PathVariable Long auctionItemId) {
        return localCacheTimer.record(() -> {
            WatchListBulkUseCase.ProcessResult result = localCacheService.processAuctionStart(auctionItemId, "");
            return ResponseEntity.ok(Map.of(
                    "strategy", "Local Cache",
                    "auctionItemId", auctionItemId,
                    "userCount", result.userCount(),
                    "queryCount", result.queryCount(),
                    "durationMs", result.durationMs()
            ));
        });
    }

    @GetMapping("/compare/{auctionItemId}")
    @Operation(summary = "3가지 방식 비교", description = "동일 데이터로 3가지 방식 순차 실행 및 결과 비교")
    public ResponseEntity<Map<String, Object>> compareAll(@PathVariable Long auctionItemId) {
        List<Map<String, Object>> results = new ArrayList<>();

        // 1. DB Bulk
        WatchListBulkUseCase.ProcessResult bulkResult = bulkService.processAuctionStart(auctionItemId, "");
        results.add(Map.of(
                "strategy", "DB Bulk",
                "userCount", bulkResult.userCount(),
                "queryCount", bulkResult.queryCount(),
                "durationMs", bulkResult.durationMs()
        ));

        // 2. Redis Set
        WatchListBulkUseCase.ProcessResult redisResult = redisService.processAuctionStart(auctionItemId, "");
        results.add(Map.of(
                "strategy", "Redis Set",
                "userCount", redisResult.userCount(),
                "queryCount", redisResult.queryCount(),
                "durationMs", redisResult.durationMs()
        ));

        // 3. Local Cache
        WatchListBulkUseCase.ProcessResult localResult = localCacheService.processAuctionStart(auctionItemId, "");
        results.add(Map.of(
                "strategy", "Local Cache",
                "userCount", localResult.userCount(),
                "queryCount", localResult.queryCount(),
                "durationMs", localResult.durationMs()
        ));

        // 결과 분석
        String fastest = results.stream()
                .min((a, b) -> Long.compare((Long) a.get("durationMs"), (Long) b.get("durationMs")))
                .map(r -> (String) r.get("strategy"))
                .orElse("Unknown");

        return ResponseEntity.ok(Map.of(
                "auctionItemId", auctionItemId,
                "results", results,
                "fastest", fastest,
                "conclusion", String.format("%s 방식이 가장 빠릅니다.", fastest)
        ));
    }

    // ========== OOM 테스트 ==========

    @PostMapping("/oom-test/add")
    @Operation(summary = "OOM 테스트 - 데이터 추가", description = "제한 없는 캐시에 대량 데이터 추가 (주의: OOM 발생 가능)")
    public ResponseEntity<Map<String, Object>> addOomTestData(
            @RequestParam(defaultValue = "1000") int auctionCount,
            @RequestParam(defaultValue = "1000") int usersPerAuction) {

        long startAuctionId = System.currentTimeMillis(); // 고유 ID 생성

        for (int i = 0; i < auctionCount; i++) {
            Long auctionItemId = startAuctionId + i;
            List<Long> userIds = LongStream.rangeClosed(1, usersPerAuction).boxed().toList();
            localCacheService.addToUnlimitedCache(auctionItemId, userIds);
        }

        WatchListLocalCacheUseCase.MemoryStats stats = localCacheService.getMemoryStats();

        return ResponseEntity.ok(Map.of(
                "addedAuctions", auctionCount,
                "usersPerAuction", usersPerAuction,
                "totalEntries", (long) auctionCount * usersPerAuction,
                "memoryStats", stats.toSummary(),
                "usageRatio", String.format("%.1f%%", stats.usageRatio() * 100),
                "warning", stats.usageRatio() > 0.8 ? "메모리 사용량 80% 초과! OOM 위험!" : "정상"
        ));
    }

    @GetMapping("/oom-test/status")
    @Operation(summary = "OOM 테스트 - 메모리 상태 확인", description = "현재 메모리 사용량 및 캐시 크기 확인")
    public ResponseEntity<Map<String, Object>> getOomTestStatus() {
        WatchListLocalCacheUseCase.MemoryStats stats = localCacheService.getMemoryStats();

        return ResponseEntity.ok(Map.of(
                "usedMemoryMb", stats.usedMemoryMb(),
                "maxMemoryMb", stats.maxMemoryMb(),
                "usageRatio", String.format("%.1f%%", stats.usageRatio() * 100),
                "limitedCacheSize", stats.limitedCacheSize(),
                "unlimitedCacheSize", stats.unlimitedCacheSize(),
                "summary", stats.toSummary()
        ));
    }

    @DeleteMapping("/oom-test/clear")
    @Operation(summary = "OOM 테스트 - 캐시 초기화", description = "모든 Local Cache 데이터 삭제")
    public ResponseEntity<Map<String, Object>> clearOomTestData() {
        localCacheService.clearCache();
        System.gc(); // GC 요청 (힌트)

        WatchListLocalCacheUseCase.MemoryStats stats = localCacheService.getMemoryStats();

        return ResponseEntity.ok(Map.of(
                "message", "캐시가 초기화되었습니다.",
                "memoryStats", stats.toSummary()
        ));
    }

    // ========== 정보 ==========

    @GetMapping("/info")
    @Operation(summary = "테스트 방법 안내", description = "각 방식별 테스트 방법 및 엔드포인트 안내")
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
                "description", "WatchList 알림 최적화 3가지 방식 비교 테스트",
                "testSteps", List.of(
                        "1. POST /setup/{auctionItemId}?userCount=1000 으로 테스트 데이터 준비",
                        "2. GET /compare/{auctionItemId} 로 3가지 방식 비교",
                        "3. 개별 테스트: /bulk/{id}, /redis/{id}, /local-cache/{id}",
                        "4. Grafana에서 watchlist.optimization.*.duration 메트릭 확인"
                ),
                "endpoints", Map.of(
                        "setup", "POST /api/test/watchlist/optimization/setup/{auctionItemId}?userCount=1000",
                        "compare", "GET /api/test/watchlist/optimization/compare/{auctionItemId}",
                        "bulk", "GET /api/test/watchlist/optimization/bulk/{auctionItemId}",
                        "redis", "GET /api/test/watchlist/optimization/redis/{auctionItemId}",
                        "localCache", "GET /api/test/watchlist/optimization/local-cache/{auctionItemId}",
                        "oomTest", "POST /api/test/watchlist/optimization/oom-test/add?auctionCount=1000&usersPerAuction=1000"
                ),
                "expectedResults", Map.of(
                        "bulk", "2 queries (1 SELECT + 1 UPDATE)",
                        "redis", "0 DB queries (Redis only)",
                        "localCache", "0 queries (Memory only, fastest)"
                ),
                "metrics", List.of(
                        "watchlist_optimization_bulk_duration_seconds",
                        "watchlist_optimization_redis_duration_seconds",
                        "watchlist_optimization_localcache_duration_seconds"
                )
        ));
    }
}
