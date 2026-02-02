package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.application.service.OrderCompleteUseCase;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import com.fourtune.auction.boundedContext.auction.port.out.OrderRepository;
import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.auction.global.security.jwt.JwtTokenProvider;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktbWluLTI1Ni1iaXRzLXJlcXVpcmVkLWZvci1obWFjLXNoYTI1Ni1hbGdvcml0aG0tdGVzdC1rZXktMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=",
    "jwt.access-token-expiration=3600000",
    "jwt.refresh-token-expiration=1209600000",
    "spring.data.elasticsearch.repositories.enabled=false"
})
class ApiV1AuctionControllerBuyNowIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuctionItemRepository auctionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderCompleteUseCase orderCompleteUseCase;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private com.google.firebase.messaging.FirebaseMessaging firebaseMessaging;

    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.in.event.AuctionItemIndexEventListener auctionItemIndexEventListener;

    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.ElasticsearchAuctionItemIndexingHandler elasticsearchAuctionItemIndexingHandler;

    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.repository.SearchAuctionItemCrudRepository searchAuctionItemCrudRepository;

    private AuctionItem activeAuction;
    private User seller;
    private User buyer;
    private String buyerToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        // 테스트용 사용자 생성
        seller = User.builder()
                .email("seller@test.com")
                .nickname("판매자")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        seller = userRepository.save(seller);

        buyer = User.builder()
                .email("buyer@test.com")
                .nickname("구매자")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        buyer = userRepository.save(buyer);

        // JWT 토큰 생성
        buyerToken = jwtTokenProvider.createAccessToken(buyer);

        // 테스트용 ACTIVE 상태 경매 생성 (즉시구매 활성화)
        activeAuction = AuctionItem.builder()
                .sellerId(seller.getId())
                .title("테스트 경매")
                .description("테스트 설명")
                .category(Category.ELECTRONICS)
                .status(AuctionStatus.ACTIVE)
                .startPrice(BigDecimal.valueOf(50000))
                .currentPrice(BigDecimal.valueOf(50000))
                .bidUnit(1000)
                .buyNowPrice(BigDecimal.valueOf(100000))
                .buyNowEnabled(true)
                .auctionStartTime(LocalDateTime.now().minusHours(1))
                .auctionEndTime(LocalDateTime.now().plusHours(1))
                .build();
        activeAuction = auctionRepository.save(activeAuction);
    }

    @Test
    @DisplayName("TC-2-1: 정상 즉시구매 성공")
    void testBuyNow_Success() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/auctions/{id}/buy-now", activeAuction.getId())
                        .header("Authorization", "Bearer " + buyerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString()); // orderId 반환

        // Then: 경매 상태 변경
        AuctionItem updatedAuction = auctionRepository.findById(activeAuction.getId())
                .orElseThrow();
        assertThat(updatedAuction.getStatus()).isEqualTo(AuctionStatus.SOLD_BY_BUY_NOW);

        // 주문 생성 확인
        Order order = orderRepository.findByAuctionId(activeAuction.getId())
                .orElseThrow(() -> new AssertionError("주문이 생성되지 않았습니다"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(order.getOrderName()).contains("[즉시구매]");
    }

    @Test
    @DisplayName("TC-2-3: 즉시구매 비활성화로 실패")
    void testBuyNow_Fail_NotEnabled() throws Exception {
        // Given: 즉시구매 비활성화된 경매 생성
        activeAuction = AuctionItem.builder()
                .sellerId(seller.getId())
                .title("테스트 경매")
                .description("테스트 설명")
                .category(Category.ELECTRONICS)
                .status(AuctionStatus.ACTIVE)
                .startPrice(BigDecimal.valueOf(50000))
                .currentPrice(BigDecimal.valueOf(50000))
                .bidUnit(1000)
                .buyNowPrice(BigDecimal.valueOf(100000))
                .buyNowEnabled(false) // 비활성화
                .auctionStartTime(LocalDateTime.now().minusHours(1))
                .auctionEndTime(LocalDateTime.now().plusHours(1))
                .build();
        activeAuction = auctionRepository.save(activeAuction);

        // When & Then
        mockMvc.perform(post("/api/v1/auctions/{id}/buy-now", activeAuction.getId())
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isBadRequest());

        // DB 검증
        AuctionItem unchangedAuction = auctionRepository.findById(activeAuction.getId())
                .orElseThrow();
        assertThat(unchangedAuction.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        assertThat(orderRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("즉시구매 후 결제 실패(failPayment) 시 경매가 ACTIVE로 복구된다")
    void testBuyNow_FailPayment_ThenAuctionRestoredToActive() throws Exception {
        // Given: 즉시구매 실행
        String orderId = mockMvc.perform(post("/api/v1/auctions/{id}/buy-now", activeAuction.getId())
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        orderId = orderId.replace("\"", "").trim();

        AuctionItem afterBuyNow = auctionRepository.findById(activeAuction.getId()).orElseThrow();
        assertThat(afterBuyNow.getStatus()).isEqualTo(AuctionStatus.SOLD_BY_BUY_NOW);

        // When: 결제 실패 처리 (즉시구매 실패 시나리오)
        orderCompleteUseCase.failPayment(orderId, "test failure");

        // Then: 경매가 ACTIVE로 복구됨
        AuctionItem afterFail = auctionRepository.findById(activeAuction.getId()).orElseThrow();
        assertThat(afterFail.getStatus()).isEqualTo(AuctionStatus.ACTIVE);

        Order order = orderRepository.findByOrderId(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("이미 즉시구매된 경매(SOLD_BY_BUY_NOW)에 즉시구매 요청 시 BN006 에러가 반환된다")
    void testBuyNow_AlreadySoldByBuyNow_ReturnsBN006() throws Exception {
        // Given: 이미 즉시구매된 경매
        activeAuction = AuctionItem.builder()
                .sellerId(seller.getId())
                .title("테스트 경매")
                .description("테스트 설명")
                .category(Category.ELECTRONICS)
                .status(AuctionStatus.SOLD_BY_BUY_NOW)
                .startPrice(BigDecimal.valueOf(50000))
                .currentPrice(BigDecimal.valueOf(100000))
                .bidUnit(1000)
                .buyNowPrice(BigDecimal.valueOf(100000))
                .buyNowEnabled(true)
                .auctionStartTime(LocalDateTime.now().minusHours(1))
                .auctionEndTime(LocalDateTime.now().plusHours(1))
                .build();
        activeAuction = auctionRepository.save(activeAuction);

        // When & Then
        mockMvc.perform(post("/api/v1/auctions/{id}/buy-now", activeAuction.getId())
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BN006"));

        AuctionItem unchanged = auctionRepository.findById(activeAuction.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(AuctionStatus.SOLD_BY_BUY_NOW);
    }
}
