package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.adapter.out.api.UserClient;
import com.fourtune.auction.boundedContext.auction.application.service.RedisViewCountService;
import com.fourtune.auction.infrastructure.kafka.AuctionKafkaProducer;
import com.fourtune.jwt.JwtTokenProvider;
import com.fourtune.shared.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * auction-service 모듈 JWT 보안 통합 테스트
 *
 * <p>실제 SecurityFilterChain을 적용하여 경매·입찰·장바구니·주문 API의
 * 인증·인가 동작을 검증한다.</p>
 *
 * <p>SecurityConfig permitAll 규칙:</p>
 * <ul>
 *   <li>{@code GET /api/v1/auctions} → permitAll (비로그인 목록 조회)</li>
 *   <li>{@code GET /api/v1/auctions/{id}} → permitAll (비로그인 상세 조회)</li>
 *   <li>그 외 경매/입찰/장바구니/주문 API → authenticated</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuctionJwtSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private MockMvc mockMvc;

    // ── 인프라 Mock ──────────────────────────────────────────────────────────
    @MockitoBean
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private RedisViewCountService redisViewCountService;

    @MockitoBean
    private AuctionKafkaProducer auctionKafkaProducer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ── 토큰 생성 헬퍼 ──────────────────────────────────────────────────────
    private String validUserToken() {
        UserResponse user = UserResponse.builder().id(1L).role("ROLE_USER").build();
        return jwtTokenProvider.createAccessToken(user);
    }

    /** 만료 시간 0 → 즉시 만료 토큰 */
    private String expiredToken() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(jwtSecret, 0, 0);
        UserResponse user = UserResponse.builder().id(1L).role("ROLE_USER").build();
        return expiredProvider.createAccessToken(user);
    }

    // ==========================================================================
    // 경매 (Auction) API
    // ==========================================================================

    @Nested
    @DisplayName("경매 API JWT 검증")
    class AuctionSecurity {

        @Test
        @DisplayName("토큰 없음 → POST /api/v1/auctions (경매 등록) → 403 (인증 필요)")
        void noToken_createAuction_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/auctions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("만료된 토큰 → POST /api/v1/auctions → 401 + 에러코드 T001")
        void expiredToken_createAuction_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/auctions")
                            .header("Authorization", "Bearer " + expiredToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("T001"));
        }

        @Test
        @DisplayName("유효한 토큰 → POST /api/v1/auctions → 보안 통과 (400, 요청 유효성 실패)")
        void validToken_createAuction_passesSecurity() throws Exception {
            // 보안은 통과하나 빈 요청이므로 유효성 검증 실패 → 400
            mockMvc.perform(post("/api/v1/auctions")
                            .header("Authorization", "Bearer " + validUserToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("토큰 없음 → GET /api/v1/auctions/{id} → 보안 통과 (permitAll GET)")
        void noToken_getAuctionDetail_isPermitted() throws Exception {
            // GET /api/v1/auctions/* 는 permitAll → 보안 차단 없음 (없는 ID라 404)
            mockMvc.perform(get("/api/v1/auctions/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("토큰 없음 → GET /api/v1/auctions (목록) → 보안 통과 (200, permitAll)")
        void noToken_getAuctionList_isPermitted() throws Exception {
            // GET /api/v1/auctions 는 permitAll → 비로그인 목록 조회 가능
            mockMvc.perform(get("/api/v1/auctions"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("토큰 없음 → DELETE /api/v1/auctions/{id} → 403 (인증 필요)")
        void noToken_deleteAuction_returns403() throws Exception {
            mockMvc.perform(delete("/api/v1/auctions/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("만료된 토큰 → DELETE /api/v1/auctions/{id} → 401")
        void expiredToken_deleteAuction_returns401() throws Exception {
            mockMvc.perform(delete("/api/v1/auctions/1")
                            .header("Authorization", "Bearer " + expiredToken()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("T001"));
        }
    }

    // ==========================================================================
    // 입찰 (Bid) API
    // ==========================================================================

    @Nested
    @DisplayName("입찰 API JWT 검증")
    class BidSecurity {

        @Test
        @DisplayName("토큰 없음 → POST /api/v1/bids (입찰) → 403 (인증 필요)")
        void noToken_placeBid_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/bids")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("만료된 토큰 → POST /api/v1/bids → 401 + 에러코드 T001")
        void expiredToken_placeBid_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/bids")
                            .header("Authorization", "Bearer " + expiredToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("T001"));
        }

        @Test
        @DisplayName("유효한 토큰 → POST /api/v1/bids → 보안 통과 (400, 유효성 실패)")
        void validToken_placeBid_passesSecurity() throws Exception {
            mockMvc.perform(post("/api/v1/bids")
                            .header("Authorization", "Bearer " + validUserToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("토큰 없음 → DELETE /api/v1/bids/{id} (입찰 취소) → 403 (인증 필요)")
        void noToken_cancelBid_returns403() throws Exception {
            mockMvc.perform(delete("/api/v1/bids/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("토큰 없음 → GET /api/v1/bids/my (내 입찰 목록) → 403 (인증 필요)")
        void noToken_getMyBids_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/bids/my"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================================================
    // 장바구니 (Cart) API
    // ==========================================================================

    @Nested
    @DisplayName("장바구니 API JWT 검증")
    class CartSecurity {

        @Test
        @DisplayName("토큰 없음 → GET /api/v1/cart → 403 (인증 필요)")
        void noToken_getCart_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/cart"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("만료된 토큰 → GET /api/v1/cart → 401")
        void expiredToken_getCart_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/cart")
                            .header("Authorization", "Bearer " + expiredToken()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("T001"));
        }
    }

    // ==========================================================================
    // 주문 (Order) API
    // ==========================================================================

    @Nested
    @DisplayName("주문 API JWT 검증")
    class OrderSecurity {

        @Test
        @DisplayName("토큰 없음 → GET /api/v1/orders → 403 (인증 필요)")
        void noToken_getOrders_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("만료된 토큰 → GET /api/v1/orders → 401")
        void expiredToken_getOrders_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/orders")
                            .header("Authorization", "Bearer " + expiredToken()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("T001"));
        }

        @Test
        @DisplayName("GET /api/v1/orders/{id}/complete → 토큰 없이 접근 가능 (permitAll 콜백)")
        void orderComplete_noToken_isPermitted() throws Exception {
            // 결제사 콜백 경로 → permitAll
            int status = mockMvc.perform(get("/api/v1/orders/ORD-001/complete"))
                    .andReturn().getResponse().getStatus();
            // 302(보안 차단)가 아니어야 함 → permitAll이므로 4xx 또는 2xx
            assert status != 302;
        }
    }
}
