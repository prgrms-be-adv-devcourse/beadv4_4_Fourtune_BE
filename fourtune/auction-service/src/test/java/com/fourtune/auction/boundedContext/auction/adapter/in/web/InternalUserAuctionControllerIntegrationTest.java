package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 내부 API: userId 기준 진행 중(ACTIVE) 경매 존재 여부 조회.
 * 탈퇴 처리 시 Feign으로 호출되는 API 검증.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InternalUserAuctionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private AuctionItemRepository auctionItemRepository;

    @MockitoBean
    private com.fourtune.auction.adapter.out.api.UserClient userClient;
    @MockitoBean
    private com.fourtune.auction.boundedContext.auction.application.service.RedisViewCountService redisViewCountService;
    @MockitoBean
    private com.fourtune.auction.infrastructure.kafka.AuctionKafkaProducer auctionKafkaProducer;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("GET /internal/users/{userId}/active-auctions — 진행 중 경매 없으면 hasActiveAuctions false, count 0")
    void getActiveAuctions_noActive_returnsFalseAndZero() throws Exception {
        long userIdNoAuctions = 99_999L;

        mockMvc.perform(get("/internal/users/{userId}/active-auctions", userIdNoAuctions))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasActiveAuctions").value(false))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("GET /internal/users/{userId}/active-auctions — 진행 중 경매 있으면 hasActiveAuctions true, count >= 1")
    void getActiveAuctions_hasActive_returnsTrueAndCount() throws Exception {
        Long sellerId = 100L;
        AuctionItem active = AuctionItem.builder()
                .sellerId(sellerId)
                .title("진행중 경매")
                .description("설명")
                .category(com.fourtune.auction.boundedContext.auction.domain.constant.Category.ETC)
                .startPrice(BigDecimal.valueOf(5000))
                .bidUnit(500)
                .auctionStartTime(LocalDateTime.now().minusHours(1))
                .auctionEndTime(LocalDateTime.now().plusDays(1))
                .status(AuctionStatus.ACTIVE)
                .currentPrice(BigDecimal.valueOf(5000))
                .build();
        auctionItemRepository.save(active);

        mockMvc.perform(get("/internal/users/{userId}/active-auctions", sellerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasActiveAuctions").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }
}
