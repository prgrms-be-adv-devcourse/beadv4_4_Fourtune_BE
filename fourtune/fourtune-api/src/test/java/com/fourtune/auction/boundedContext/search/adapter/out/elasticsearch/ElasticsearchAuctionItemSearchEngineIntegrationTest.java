package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch;

import com.fourtune.api.infrastructure.kafka.notification.NotificationKafkaProducer;
import com.fourtune.api.infrastructure.kafka.search.SearchKafkaProducer;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListKafkaProducer;
import com.fourtune.auction.boundedContext.search.adapter.in.event.AuctionItemIndexEventListener;
import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.document.SearchAuctionItemDocument;
import com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.repository.SearchAuctionItemCrudRepository;
import com.fourtune.auction.boundedContext.search.domain.*;
import com.fourtune.auction.boundedContext.search.domain.constant.SearchSort;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ElasticSearch 검색 엔진 통합 테스트
 * - 싱글턴 ElasticsearchTestContainer를 사용 (Nori 플러그인 포함)
 * - elasticsearch/Dockerfile을 재사용하므로 ES 버전/플러그인 변경 시 Dockerfile만 수정하면 됨
 */
@SpringBootTest
@DisplayName("ElasticSearch 검색 엔진 통합 테스트")
class ElasticsearchAuctionItemSearchEngineIntegrationTest {

    static ElasticsearchContainer elasticsearch = ElasticsearchTestContainer.getInstance();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

    @MockitoBean
    private AuctionItemIndexEventListener auctionItemIndexEventListener;

    @MockitoBean
    private WatchListKafkaProducer watchListKafkaProducer;

    @MockitoBean
    private NotificationKafkaProducer notificationKafkaProducer;

    @MockitoBean
    private SearchKafkaProducer searchKafkaProducer;

    @Autowired
    private ElasticsearchAuctionItemSearchEngine searchEngine;

    @Autowired
    private SearchAuctionItemCrudRepository repository;

    @Autowired
    private ElasticsearchOperations operations;

    @BeforeEach
    void setUp() {
        // 1. 인덱스 운영 객체 가져오기
        IndexOperations indexOps = operations.indexOps(SearchAuctionItemDocument.class);

        // 2. 기존 인덱스가 있다면 삭제하고 새로 생성 (초기화)
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        indexOps.putMapping(); // 매핑 정보 적용 (날짜 포맷 등)

        // 3. 데이터베이스(리포지토리) 초기화
        repository.deleteAll();

        // 4. 리프레시를 통해 변경사항 즉시 반영
        indexOps.refresh();
    }

    @Test
    @DisplayName("제목에 포함된 키워드로 검색이 되어야 한다")
    void search_ByKeywordInTitle_ShouldReturnMatchingItems() {
        // given
        saveTestDocument("맥북 프로 16인치", "고성능 노트북입니다.", "ELECTRONICS", "ACTIVE", 2500000, 100L);
        saveTestDocument("아이패드 에어", "태블릿입니다.", "ELECTRONICS", "ACTIVE", 800000, 50L);
        saveTestDocument("맥북 에어", "가벼운 노트북", "ELECTRONICS", "ACTIVE", 1300000, 30L);
        refreshIndex(); // 중요: 저장 후 인덱스 리프레시

        SearchCondition condition = new SearchCondition(
                "맥북", // 키워드
                null, // 카테고리
                null, // 가격 범위
                null, // 상태
                SearchSort.LATEST,
                1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        assertThat(result.items()).hasSize(2)
                .extracting(SearchAuctionItemView::title)
                .containsExactlyInAnyOrder("맥북 프로 16인치", "맥북 에어");
    }

    @Test
    @DisplayName("설명(description)에 포함된 키워드로도 검색이 되어야 한다")
    void search_ByKeywordInDescription_ShouldReturnMatchingItems() {
        // given
        saveTestDocument("게이밍 노트북", "최고사양 게임용 랩탑", "ELECTRONICS", "ACTIVE", 2000000, 0L);
        saveTestDocument("사무용 마우스", "무선 마우스", "ELECTRONICS", "ACTIVE", 50000, 0L);
        refreshIndex();

        SearchCondition condition = new SearchCondition(
                "게임용",
                null, null, null,
                SearchSort.LATEST, 1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("게이밍 노트북");
    }

    @Test
    @DisplayName("Nori 분석기: 복합어 및 조사가 포함된 한국어 검색이 정상 작동해야 한다")
    void search_ByNoriAnalyzer_ShouldHandleKoreanMorphology() {
        // given
        // 1. 복합어 (붙여쓰기)
        saveTestDocument("맥북프로", "최신형 노트북", "ELECTRONICS", "ACTIVE", 2000000, 0L);
        // 2. 조사 포함된 데이터
        saveTestDocument("아이폰이 예뻐요", "스마트폰", "ELECTRONICS", "ACTIVE", 1200000, 0L);
        // 3. 복합어 (삼성전자)
        saveTestDocument("삼성전자 갤럭시북", "노트북", "ELECTRONICS", "ACTIVE", 1500000, 0L);
        refreshIndex();

        // Case 1: 복합어 분해 검색 ("맥북"으로 "맥북프로" 찾기)
        SearchCondition cond1 = new SearchCondition("맥북", null, null, null, SearchSort.LATEST, 1);
        SearchResultPage<SearchAuctionItemView> res1 = searchEngine.search(cond1);
        assertThat(res1.items()).extracting(SearchAuctionItemView::title).contains("맥북프로");

        // Case 2: 조사 제거 검색 ("아이폰"으로 "아이폰이 예뻐요" 찾기)
        SearchCondition cond2 = new SearchCondition("아이폰", null, null, null, SearchSort.LATEST, 1);
        SearchResultPage<SearchAuctionItemView> res2 = searchEngine.search(cond2);
        assertThat(res2.items()).extracting(SearchAuctionItemView::title).contains("아이폰이 예뻐요");

        // Case 3: 복합어 내부 키워드 검색 ("삼성"으로 "삼성전자 갤럭시북" 찾기)
        SearchCondition cond3 = new SearchCondition("삼성", null, null, null, SearchSort.LATEST, 1);
        SearchResultPage<SearchAuctionItemView> res3 = searchEngine.search(cond3);
        assertThat(res3.items()).extracting(SearchAuctionItemView::title).contains("삼성전자 갤럭시북");
    }

    @Test
    @DisplayName("특정 카테고리에 속한 상품만 필터링되어야 한다")
    void search_ByCategory_ShouldReturnOnlyMatchingCategory() {
        // given
        saveTestDocument("노트북", "전자기기", "ELECTRONICS", "ACTIVE", 1000000, 0L);
        saveTestDocument("청바지", "의류", "CLOTHING", "ACTIVE", 30000, 0L);
        saveTestDocument("도자기", "예술품", "POTTERY", "ACTIVE", 100000, 0L);
        refreshIndex();

        SearchCondition condition = new SearchCondition(
                null,
                Set.of("ELECTRONICS"), // 전자기기만 조회
                null, null,
                SearchSort.LATEST, 1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).category()).isEqualTo("ELECTRONICS");
    }

    @Test
    @DisplayName("가격 범위(최소~최대) 내의 상품만 조회되어야 한다")
    void search_ByPriceRange_ShouldReturnItemsInRange() {
        // given
        saveTestDocument("저가형", "설명", "ELECTRONICS", "ACTIVE", 10000, 0L);
        saveTestDocument("중가형", "설명", "ELECTRONICS", "ACTIVE", 50000, 0L);
        saveTestDocument("고가형", "설명", "ELECTRONICS", "ACTIVE", 100000, 0L);
        refreshIndex();

        SearchCondition condition = new SearchCondition(
                null, null,
                new SearchPriceRange(BigDecimal.valueOf(30000), BigDecimal.valueOf(80000)), // 3만 ~ 8만
                null,
                SearchSort.LATEST, 1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("중가형");
        assertThat(result.items().get(0).currentPrice()).isEqualByComparingTo(BigDecimal.valueOf(50000));
    }

    @Test
    @DisplayName("상품 상태(ACTIVE, ENDED 등)로 필터링되어야 한다")
    void search_ByStatus_ShouldReturnOnlyMatchingStatus() {
        // given
        saveTestDocument("진행중 경매", "설명", "ELECTRONICS", "ACTIVE", 100000, 0L);
        saveTestDocument("종료된 경매", "설명", "ELECTRONICS", "ENDED", 100000, 0L);
        saveTestDocument("예정된 경매", "설명", "ELECTRONICS", "SCHEDULED", 100000, 0L);
        refreshIndex();

        SearchCondition condition = new SearchCondition(
                null, null, null,
                Set.of("ACTIVE", "SCHEDULED"), // 진행중이거나 예정된 것만
                SearchSort.LATEST, 1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        assertThat(result.items()).hasSize(2)
                .extracting(SearchAuctionItemView::status)
                .containsExactlyInAnyOrder("ACTIVE", "SCHEDULED");
    }

    @Test
    @DisplayName("복합 조건(키워드+카테고리+가격+상태) 검색이 정확히 동작해야 한다")
    void search_WithMultipleConditions_ShouldReturnMatchingItems() {
        // given
        // 조건에 맞는 것
        saveTestDocument("맥북 프로", "상태 좋음", "ELECTRONICS", "ACTIVE", 2000000, 100L);

        // 키워드 불일치
        saveTestDocument("갤럭시 북", "상태 좋음", "ELECTRONICS", "ACTIVE", 2000000, 100L);
        // 카테고리 불일치
        saveTestDocument("맥북 파우치", "맥북용", "CLOTHING", "ACTIVE", 50000, 10L);
        // 가격 범위 불일치
        saveTestDocument("맥북 에어", "저렴함", "ELECTRONICS", "ACTIVE", 1000000, 50L); // 가격 미달
        // 상태 불일치
        saveTestDocument("맥북 프로 구형", "고장남", "ELECTRONICS", "ENDED", 2000000, 10L);

        refreshIndex();

        SearchCondition condition = new SearchCondition(
                "맥북",
                Set.of("ELECTRONICS"),
                new SearchPriceRange(BigDecimal.valueOf(1500000), BigDecimal.valueOf(3000000)),
                Set.of("ACTIVE"),
                SearchSort.LATEST, 1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("맥북 프로");
    }

    @Test
    @DisplayName("인기순(조회수) 정렬이 정상 작동해야 한다")
    void search_SortByPopularity_ShouldReturnItemsInOrder() {
        // given
        saveTestDocument("인기 없음", "조회수 0", "ELECTRONICS", "ACTIVE", 10000, 0L);
        saveTestDocument("인기 많음", "조회수 100", "ELECTRONICS", "ACTIVE", 10000, 100L);
        saveTestDocument("인기 보통", "조회수 50", "ELECTRONICS", "ACTIVE", 10000, 50L);
        refreshIndex();

        SearchCondition condition = new SearchCondition(
                null, null, null, null,
                SearchSort.POPULAR, // 인기순 정렬
                1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        result.items()
                .forEach(item -> System.out.println("Result Item: " + item.title() + ", View: " + item.viewCount()));

        assertThat(result.items()).hasSize(3);
        assertThat(result.items().get(0).title()).isEqualTo("인기 많음");
        assertThat(result.items().get(1).title()).isEqualTo("인기 보통");
        assertThat(result.items().get(2).title()).isEqualTo("인기 없음");
    }

    @Test
    @DisplayName("조회수가 같을 경우 최신 상품이 더 상위에 노출되어야 한다")
    void search_SortByPopularity_SameViewCount_NewerFirst() {
        // given
        LocalDateTime now = LocalDateTime.now();
        // A: 100 views, created NOW
        saveTestDocument("New Item", "New", "ELECTRONICS", "ACTIVE", 10000, 100L, now);
        // B: 100 views, created 1 day ago
        saveTestDocument("Old Item", "Old", "ELECTRONICS", "ACTIVE", 10000, 100L, now.minusDays(1));
        refreshIndex();

        SearchCondition condition = new SearchCondition(
                null, null, null, null,
                SearchSort.POPULAR,
                1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).title()).isEqualTo("New Item");
        assertThat(result.items().get(1).title()).isEqualTo("Old Item");
    }

    @Test
    @DisplayName("신선도 감가(Time Decay)가 작동하여 오래된 인기 상품보다 최신 비인기 상품이 상위일 수 있다")
    void search_SortByPopularity_FreshnessDecay() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // 오래된 인기상품: 조회수 1000, 24시간 전 생성
        // Base Score ~= log10(1001) ~= 3.0
        // Decay ~= sqrt(24 + 2) ~= 5.1
        // 최종값 ~= 0.58
        saveTestDocument("인기있지만 오래됨", "오래된 상품", "ELECTRONICS", "ACTIVE", 10000, 1000L, now.minusHours(24));

        // 새 상품: 조회수 10, 지금 생성
        // Base Score ~= log10(11) ~= 1.04
        // Decay ~= sqrt(0 + 2) ~= 1.41
        // 최종값 ~= 0.73
        saveTestDocument("일반적인 새 상품", "새 상품", "ELECTRONICS", "ACTIVE", 10000, 10L, now);

        refreshIndex();

        SearchCondition condition = new SearchCondition(
                null, null, null, null,
                SearchSort.POPULAR,
                1);

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).title()).isEqualTo("일반적인 새 상품"); // 0.73 > 0.58
        assertThat(result.items().get(1).title()).isEqualTo("인기있지만 오래됨");
    }

    @Test
    @DisplayName("페이징 처리가 정확해야 한다 (2페이지 조회)")
    void search_WithPaging_ShouldReturnCorrectPage() {
        // given
        // 25개 데이터 생성
        for (int i = 1; i <= 25; i++) {
            saveTestDocument("상품 " + i, "설명", "ELECTRONICS", "ACTIVE", 10000, (long) i);
        }
        refreshIndex();

        // 페이지 크기가 기본 20이라고 가정 (application.yml 설정에 따름, 테스트환경 디폴트 체크 필요)
        // 여기서는 items 사이즈로 검증
        SearchCondition condition = new SearchCondition(
                null, null, null, null,
                SearchSort.LATEST,
                2 // 2페이지 요청
        );

        // when
        SearchResultPage<SearchAuctionItemView> result = searchEngine.search(condition);

        // then
        // 총 25개 중 1페이지 20개, 2페이지 5개 예상
        assertThat(result.totalElements()).isEqualTo(25);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.items()).hasSize(5);
        assertThat(result.hasNext()).isFalse(); // 마지막 페이지이므로 false
    }

    private void saveTestDocument(String title, String description, String category,
            String status, int price, Long viewCount) {
        saveTestDocument(title, description, category, status, price, viewCount, LocalDateTime.now());
    }

    private void saveTestDocument(String title, String description, String category,
            String status, int price, Long viewCount, LocalDateTime createdAt) {
        SearchAuctionItemDocument doc = SearchAuctionItemDocument.builder()
                .auctionItemId(System.nanoTime())
                .title(title)
                .description(description)
                .category(category)
                .status(status)
                .startPrice(BigDecimal.valueOf(price))
                .currentPrice(BigDecimal.valueOf(price))
                .startAt(createdAt.atZone(java.time.ZoneId.of("Asia/Seoul")))
                .endAt(createdAt.plusDays(7).atZone(java.time.ZoneId.of("Asia/Seoul")))
                .thumbnailUrl("https://example.com/image.jpg")
                .createdAt(createdAt.atZone(java.time.ZoneId.of("Asia/Seoul"))) // 최신순 정렬 시 이 값이 중요
                .updatedAt(createdAt.atZone(java.time.ZoneId.of("Asia/Seoul")))
                .viewCount(viewCount)
                .watchlistCount(0)
                .bidCount(0)
                .build();

        repository.save(doc);
    }

    private void refreshIndex() {
        // 인덱스 강제 리프레시 (검색 가능 상태로 만듦)
        operations.indexOps(SearchAuctionItemDocument.class).refresh();
    }
}
