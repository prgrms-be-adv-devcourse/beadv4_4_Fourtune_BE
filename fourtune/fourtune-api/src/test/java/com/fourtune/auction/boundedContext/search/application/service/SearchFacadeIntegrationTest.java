package com.fourtune.auction.boundedContext.search.application.service;

import com.fourtune.auction.boundedContext.notification.adapter.in.kafka.NotificationUserKafkaListener;
import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.document.SearchAuctionItemDocument;
import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.repository.SearchAuctionItemCrudRepository;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.boundedContext.search.domain.SearchCondition;
import com.fourtune.auction.boundedContext.search.domain.constant.SearchSort;
import com.fourtune.auction.boundedContext.search.domain.SearchResultPage;
import com.fourtune.auction.boundedContext.settlement.adapter.in.kafka.SettlementUserKafkaListener;
import com.fourtune.auction.boundedContext.watchList.adapter.in.kafka.WatchListUserKafkaListener;
import com.fourtune.common.global.config.FirebaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@DisplayName("SearchFacade 통합 테스트 (ES + Redis)")
class SearchFacadeIntegrationTest {

    // 1. Elasticsearch Container
    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:9.2.3") // 프로젝트 버전과 일치 확인 필요 (기존 테스트 참고함)
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .withStartupTimeout(Duration.ofMinutes(5));

    // 2. Redis Container
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    // Mock External Dependencies to prevent context load failure
    @MockitoBean private WatchListUserKafkaListener watchListUserKafkaListener;
    @MockitoBean private SettlementUserKafkaListener settlementUserKafkaListener;
    @MockitoBean private NotificationUserKafkaListener notificationUserKafkaListener;
    @MockitoBean private FirebaseConfig firebaseConfig;
    @MockitoBean private com.google.firebase.messaging.FirebaseMessaging firebaseMessaging;

    @Autowired
    private SearchFacade searchFacade;

    @Autowired
    private SearchAuctionItemCrudRepository repository;

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        // ES Index Init
        IndexOperations indexOps = operations.indexOps(SearchAuctionItemDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        indexOps.putMapping();
        repository.deleteAll();
        indexOps.refresh();

        // Redis Flush
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("검색 시 ES에서 결과를 반환하고 Redis에 최근 검색어를 저장해야 한다")
    void search_ShouldReturnResults_And_SaveRecentKeyword() {
        // given
        // 1. ES 데이터 준비
        saveTestDocument("Integration Test Item", "Test Desc", "ELECTRONICS", "ACTIVE", 10000);
        operations.indexOps(SearchAuctionItemDocument.class).refresh();

        Long userId = 1L;
        String keyword = "Integration";
        SearchCondition condition = new SearchCondition(keyword, null, null, null, SearchSort.LATEST, 1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchFacade.search(userId, condition);

        // then
        // 1. Check Search Results (from ES)
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("Integration Test Item");

        // 2. Check Recent Keyword (from Redis) - Async execution check with Awaitility
        String redisKey = "recent_search:" + userId;
        
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            Set<String> savedKeywords = redisTemplate.opsForZSet().reverseRange(redisKey, 0, -1);
            assertThat(savedKeywords).contains(keyword);
        });
    }

    private void saveTestDocument(String title, String description, String category, String status, int price) {
        LocalDateTime now = LocalDateTime.now();
        SearchAuctionItemDocument doc = SearchAuctionItemDocument.builder()
                .auctionItemId(System.nanoTime())
                .title(title)
                .description(description)
                .category(category)
                .status(status)
                .startPrice(BigDecimal.valueOf(price))
                .currentPrice(BigDecimal.valueOf(price))
                .startAt(now.atZone(java.time.ZoneId.of("Asia/Seoul")))
                .endAt(now.plusDays(7).atZone(java.time.ZoneId.of("Asia/Seoul")))
                .thumbnailUrl("https://example.com/image.jpg")
                .createdAt(now.atZone(java.time.ZoneId.of("Asia/Seoul")))
                .updatedAt(now.atZone(java.time.ZoneId.of("Asia/Seoul")))
                .viewCount(0L)
                .watchlistCount(0)
                .bidCount(0)
                .build();
        repository.save(doc);
    }
}
