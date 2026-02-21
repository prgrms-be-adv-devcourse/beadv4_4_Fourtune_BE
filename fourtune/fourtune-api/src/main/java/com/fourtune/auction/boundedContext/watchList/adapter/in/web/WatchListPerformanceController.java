package com.fourtune.auction.boundedContext.watchList.adapter.in.web;

import com.fourtune.auction.boundedContext.watchList.domain.WatchList;
import com.fourtune.auction.boundedContext.watchList.mapper.WatchListMapper;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListRepository;
import com.fourtune.shared.watchList.dto.WatchListResponseDto;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * WatchList 로딩 전략별 성능 테스트용 컨트롤러
 *
 * 테스트 목적:
 * 1. LAZY 로딩 (기본) - N+1 문제 발생 가능
 * 2. Fetch Join - 한 번의 쿼리로 연관 엔티티 함께 조회
 * 3. EntityGraph - 선언적으로 즉시 로딩 설정
 *
 * 프로필 제한:
 * - local, dev: 수동 성능 테스트용으로 활성화
 * - test: 단위/통합 테스트 간섭 방지를 위해 비활성화
 * - prod: 보안상 내부 메트릭 노출 방지를 위해 비활성화
 */
@RestController
@RequestMapping("/api/test/watchlist/performance")
@RequiredArgsConstructor
@Slf4j
@Profile({"local", "dev"})
@Validated
@Tag(name = "WatchList Performance Test", description = "로딩 전략별 성능 테스트 API (테스트용)")
public class WatchListPerformanceController {

    private final WatchListRepository watchListRepository;
    private final MeterRegistry meterRegistry;

    private Timer lazyTimer;
    private Timer fetchJoinTimer;
    private Timer entityGraphTimer;
    private DistributionSummary lazyResultSummary;
    private DistributionSummary fetchJoinResultSummary;
    private DistributionSummary entityGraphResultSummary;

    @PostConstruct
    public void initMetrics() {
        lazyTimer = Timer.builder("watchlist.lazy.duration")
                .description("LAZY 로딩 방식 응답 시간")
                .tag("strategy", "lazy")
                .register(meterRegistry);

        fetchJoinTimer = Timer.builder("watchlist.fetchjoin.duration")
                .description("Fetch Join 방식 응답 시간")
                .tag("strategy", "fetch_join")
                .register(meterRegistry);

        entityGraphTimer = Timer.builder("watchlist.entitygraph.duration")
                .description("EntityGraph 방식 응답 시간")
                .tag("strategy", "entity_graph")
                .register(meterRegistry);

        lazyResultSummary = DistributionSummary.builder("watchlist.lazy.result.size")
                .description("LAZY 로딩 결과 건수 분포")
                .tag("strategy", "lazy")
                .register(meterRegistry);

        fetchJoinResultSummary = DistributionSummary.builder("watchlist.fetchjoin.result.size")
                .description("Fetch Join 결과 건수 분포")
                .tag("strategy", "fetch_join")
                .register(meterRegistry);

        entityGraphResultSummary = DistributionSummary.builder("watchlist.entitygraph.result.size")
                .description("EntityGraph 결과 건수 분포")
                .tag("strategy", "entity_graph")
                .register(meterRegistry);
    }

    /**
     * LAZY 로딩 방식 테스트
     *
     * 특징:
     * - 기본 로딩 전략
     * - 연관 엔티티 접근 시 추가 쿼리 발생 (N+1 문제)
     * - WatchList 10개 조회 시: 1(목록) + 10(user) + 10(auctionItem) = 21개 쿼리
     */
    @GetMapping("/lazy/{userId}")
    @Transactional(readOnly = true)
    @Operation(summary = "LAZY 로딩 테스트", description = "기본 LAZY 로딩으로 관심목록 조회 (N+1 문제 발생)")
    public ResponseEntity<List<WatchListResponseDto>> testLazyLoading(
            @PathVariable @Positive(message = "userId는 양수여야 합니다") Long userId) {

        return lazyTimer.record(() -> {
            log.info("[LAZY] 사용자 {} 관심목록 조회 시작", userId);

            // LAZY 로딩: findAllByUserId 호출 시 WatchList만 조회
            // WatchListResponseDto.from() 호출 시 user, auctionItem 추가 쿼리 발생
            List<WatchList> watchLists = watchListRepository.findAllByUserId(userId);

            // 여기서 N+1 문제 발생: 각 WatchList마다 user, auctionItem 조회
            List<WatchListResponseDto> result = watchLists.stream()
                    .map(WatchListMapper::from)
                    .toList();

            lazyResultSummary.record(result.size());
            log.info("[LAZY] 사용자 {} 관심목록 조회 완료: {}건", userId, result.size());

            return ResponseEntity.ok(result);
        });
    }

    /**
     * Fetch Join 방식 테스트
     *
     * 특징:
     * - JPQL JOIN FETCH 사용
     * - 한 번의 쿼리로 모든 연관 엔티티 조회
     * - WatchList 10개 조회 시: 1개 쿼리
     * - 단점: 페이징과 함께 사용 시 주의 필요 (in-memory 페이징)
     */
    @GetMapping("/fetch-join/{userId}")
    @Transactional(readOnly = true)
    @Operation(summary = "Fetch Join 테스트", description = "JPQL Fetch Join으로 관심목록 조회 (쿼리 1회)")
    public ResponseEntity<List<WatchListResponseDto>> testFetchJoin(
            @PathVariable @Positive(message = "userId는 양수여야 합니다") Long userId) {

        return fetchJoinTimer.record(() -> {
            log.info("[FETCH JOIN] 사용자 {} 관심목록 조회 시작", userId);

            // Fetch Join: 한 번의 쿼리로 WatchList + user + auctionItem 모두 조회
            List<WatchList> watchLists = watchListRepository.findAllByUserIdWithFetchJoin(userId);

            // 이미 모든 데이터가 로드되어 있어 추가 쿼리 없음
            List<WatchListResponseDto> result = watchLists.stream()
                    .map(WatchListMapper::from)
                    .toList();

            fetchJoinResultSummary.record(result.size());
            log.info("[FETCH JOIN] 사용자 {} 관심목록 조회 완료: {}건", userId, result.size());

            return ResponseEntity.ok(result);
        });
    }

    /**
     * EntityGraph 방식 테스트
     *
     * 특징:
     * - @EntityGraph 어노테이션으로 선언적 즉시 로딩
     * - 한 번의 쿼리로 모든 연관 엔티티 조회
     * - Fetch Join과 유사하지만 더 선언적
     * - 동적으로 로딩 전략 변경 가능
     */
    @GetMapping("/entity-graph/{userId}")
    @Transactional(readOnly = true)
    @Operation(summary = "EntityGraph 테스트", description = "@EntityGraph로 관심목록 조회 (쿼리 1회)")
    public ResponseEntity<List<WatchListResponseDto>> testEntityGraph(
            @PathVariable @Positive(message = "userId는 양수여야 합니다") Long userId) {

        return entityGraphTimer.record(() -> {
            log.info("[ENTITY GRAPH] 사용자 {} 관심목록 조회 시작", userId);

            // EntityGraph: @EntityGraph로 지정된 연관 엔티티를 한 번에 조회
            List<WatchList> watchLists = watchListRepository.findWithGraphByUserId(userId);

            // 이미 모든 데이터가 로드되어 있어 추가 쿼리 없음
            List<WatchListResponseDto> result = watchLists.stream()
                    .map(WatchListMapper::from)
                    .toList();

            entityGraphResultSummary.record(result.size());
            log.info("[ENTITY GRAPH] 사용자 {} 관심목록 조회 완료: {}건", userId, result.size());

            return ResponseEntity.ok(result);
        });
    }

    /**
     * 성능 비교 요약 정보
     * 현재까지 수집된 메트릭 정보를 반환
     */
    @GetMapping("/summary")
    @Operation(summary = "성능 테스트 요약", description = "로딩 전략별 성능 비교 요약 정보")
    public ResponseEntity<Map<String, Object>> getPerformanceSummary() {
        return ResponseEntity.ok(Map.of(
            "description", "로딩 전략별 성능 비교",
            "strategies", Map.of(
                "lazy", Map.of(
                    "name", "LAZY 로딩",
                    "endpoint", "/api/test/watchlist/performance/lazy/{userId}",
                    "queryCount", "1 + N(user) + N(auctionItem)",
                    "advantage", "메모리 사용량 적음",
                    "disadvantage", "N+1 문제로 쿼리 수 증가"
                ),
                "fetchJoin", Map.of(
                    "name", "Fetch Join",
                    "endpoint", "/api/test/watchlist/performance/fetch-join/{userId}",
                    "queryCount", "1",
                    "advantage", "쿼리 1회로 모든 데이터 조회",
                    "disadvantage", "페이징 시 in-memory 처리"
                ),
                "entityGraph", Map.of(
                    "name", "EntityGraph",
                    "endpoint", "/api/test/watchlist/performance/entity-graph/{userId}",
                    "queryCount", "1",
                    "advantage", "선언적, 동적 로딩 전략 변경 가능",
                    "disadvantage", "Fetch Join과 유사한 제약"
                )
            ),
            "metrics", Map.of(
                "prometheus", "/actuator/prometheus",
                "customMetrics", List.of(
                    "watchlist_lazy_duration_seconds",
                    "watchlist_fetchjoin_duration_seconds",
                    "watchlist_entitygraph_duration_seconds",
                    "watchlist_lazy_result_size",
                    "watchlist_fetchjoin_result_size",
                    "watchlist_entitygraph_result_size"
                )
            ),
            "testTip", "k6 또는 JMeter로 동시 요청 테스트 후 Grafana에서 비교 분석"
        ));
    }
}
