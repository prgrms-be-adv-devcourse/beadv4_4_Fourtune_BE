package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.adapter.out.api.UserClient;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.infrastructure.kafka.AuctionKafkaProducer;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.boundedContext.auction.application.service.RedisViewCountService;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 경매 API 통합 테스트: auction-service 단독, 외부(User/Redis)는 Mock.
 * - 경매 목록·상세·조회수 API가 정상 동작하는지 검증.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApiV1AuctionControllerIntegrationTest {

        @Autowired
        private WebApplicationContext context;

        private MockMvc mockMvc;

        @Autowired
        private AuctionItemRepository auctionItemRepository;

        @MockitoBean
        private UserClient userClient;

        @MockitoBean
        private RedisViewCountService redisViewCountService;

        @MockitoBean
        private AuctionKafkaProducer auctionKafkaProducer;

        private static final Long TEST_SELLER_ID = 1L;
        private static final String TEST_SELLER_NICKNAME = "테스트판매자";
        private static final String TEST_TITLE = "통합테스트 경매 상품";

        @BeforeEach
        void setUp() {
                this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

                when(userClient.getNicknamesByIds(anyList()))
                                .thenReturn(Map.of(String.valueOf(TEST_SELLER_ID), TEST_SELLER_NICKNAME));
                when(redisViewCountService.getViewCount(any(Long.class), any(Long.class)))
                                .thenAnswer(inv -> inv.getArgument(1));

                AuctionItem item = AuctionItem.builder()
                                .sellerId(TEST_SELLER_ID)
                                .title(TEST_TITLE)
                                .description("테스트 설명")
                                .category(Category.ETC)
                                .startPrice(BigDecimal.valueOf(10_000))
                                .bidUnit(1000)
                                .auctionStartTime(LocalDateTime.now().minusHours(1))
                                .auctionEndTime(LocalDateTime.now().plusDays(1))
                                .status(AuctionStatus.ACTIVE)
                                .currentPrice(BigDecimal.valueOf(10_000))
                                .build();
                auctionItemRepository.save(item);
        }

        @Test
        @DisplayName("GET /api/v1/auctions/{id} — 경매 상세 조회 시 Mock 닉네임이 응답에 포함된다")
        void getAuctionDetail_returnsSellerNicknameFromMock() throws Exception {
                List<AuctionItem> all = auctionItemRepository.findAll();
                Long id = all.get(0).getId();

                mockMvc.perform(get("/api/v1/auctions/{id}", id))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(id))
                                .andExpect(jsonPath("$.seller_id").value(TEST_SELLER_ID))
                                .andExpect(jsonPath("$.seller_nickname").value(TEST_SELLER_NICKNAME))
                                .andExpect(jsonPath("$.title").value(TEST_TITLE))
                                .andExpect(jsonPath("$.status").value(AuctionStatus.ACTIVE.name()))
                                .andExpect(jsonPath("$.current_price").value(10000));
        }

        @Test
        @DisplayName("GET /api/v1/auctions/{id} — 존재하지 않는 ID는 404")
        void getAuctionDetail_notFound_returns404() throws Exception {
                mockMvc.perform(get("/api/v1/auctions/{id}", 99_999L))
                                .andDo(print())
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("GET /api/v1/auctions — 경매 목록 조회 시 저장한 경매가 포함된다")
        void getAuctionList_returnsSavedItems() throws Exception {
                mockMvc.perform(get("/api/v1/auctions").param("size", "10"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content.length()").value(1))
                                .andExpect(jsonPath("$.content[0].title").value(TEST_TITLE))
                                .andExpect(jsonPath("$.content[0].seller_id").value(TEST_SELLER_ID))
                                .andExpect(jsonPath("$.content[0].status").value(AuctionStatus.ACTIVE.name()));
        }

        @Test
        @WithMockUser
        @DisplayName("GET /api/v1/auctions?status=ACTIVE — ACTIVE만 조회된다")
        void getAuctionList_filterByStatus_returnsMatching() throws Exception {
                mockMvc.perform(get("/api/v1/auctions").param("status", "ACTIVE").param("size", "10"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].status").value(AuctionStatus.ACTIVE.name()));
        }

        @Test
        @WithMockUser
        @DisplayName("PATCH /api/v1/auctions/{id}/view — 조회수 증가 요청이 200으로 처리된다")
        void increaseViewCount_returns200() throws Exception {
                List<AuctionItem> all = auctionItemRepository.findAll();
                Long id = all.get(0).getId();

                mockMvc.perform(patch("/api/v1/auctions/{id}/view", id))
                                .andDo(print())
                                .andExpect(status().isOk());
        }
}
