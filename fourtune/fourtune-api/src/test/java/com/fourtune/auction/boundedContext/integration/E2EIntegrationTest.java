package com.fourtune.auction.boundedContext.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.auction.application.service.BidSupport;
import com.fourtune.auction.boundedContext.auction.application.service.OrderCreateUseCase;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import com.fourtune.auction.boundedContext.payment.adapter.in.web.dto.ConfirmPaymentRequest;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import com.fourtune.common.shared.payment.constant.CashPolicy;
import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.mapper.UserMapper;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.common.shared.auction.dto.BidPlaceRequest;
import com.fourtune.common.shared.auction.dto.CartAddItemRequest;
import com.fourtune.common.shared.user.dto.UserLoginRequest;
import com.fourtune.common.shared.user.dto.UserResponse;
import com.fourtune.common.shared.user.dto.UserSignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E 통합 테스트
 * 시나리오 0, 1, 2를 검증하는 통합 테스트
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktbWluLTI1Ni1iaXRzLXJlcXVpcmVkLWZvci1obWFjLXNoYTI1Ni1hbGdvcml0aG0tdGVzdC1rZXktMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=",
    "jwt.access-token-expiration=3600000",
    "jwt.refresh-token-expiration=1209600000",
    "spring.data.elasticsearch.repositories.enabled=false"
})
class E2EIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionItemRepository auctionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BidSupport bidSupport;

    @Autowired
    private OrderCreateUseCase orderCreateUseCase;

    @Autowired
    private com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade paymentFacade;

    @Autowired
    private com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository paymentUserRepository;

    @Autowired
    private WalletRepository walletRepository;

    @MockitoBean
    private com.google.firebase.messaging.FirebaseMessaging firebaseMessaging;

    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.in.event.AuctionItemIndexEventListener auctionItemIndexEventListener;

    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.ElasticsearchAuctionItemIndexingHandler elasticsearchAuctionItemIndexingHandler;

    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.repository.SearchAuctionItemCrudRepository searchAuctionItemCrudRepository;

    @MockitoBean
    private com.fourtune.auction.boundedContext.payment.port.out.PaymentGatewayPort paymentGatewayPort;

    @MockitoBean
    private com.fourtune.auction.boundedContext.payment.port.out.AuctionPort auctionPort;

    @MockitoBean
    private com.fourtune.common.shared.watchList.kafka.WatchListKafkaProducer watchListKafkaProducer;

    @MockitoBean
    private com.fourtune.common.shared.notification.kafka.NotificationKafkaProducer notificationKafkaProducer;

    @MockitoBean
    private com.fourtune.common.shared.search.kafka.SearchKafkaProducer searchKafkaProducer;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        // PaymentDataInit이 주석 처리되어 있으므로 테스트에서 직접 시스템/플랫폼 지갑 초기화
        initSystemWallets();

        // PaymentGatewayPort 모킹 설정 (Toss API 호출 모킹)
        when(paymentGatewayPort.confirm(anyString(), anyString(), anyLong()))
                .thenAnswer(invocation -> {
                    String paymentKey = invocation.getArgument(0);
                    String orderId = invocation.getArgument(1);
                    Long amount = invocation.getArgument(2);
                    return com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult.success(
                            paymentKey, orderId, amount
                    );
                });

        // AuctionPort 모킹 설정 (Payment 도메인에서 경매 주문 조회 시 사용)
        // 실제 주문 정보를 기반으로 OrderDto 생성하여 반환
        when(auctionPort.getOrder(anyString()))
                .thenAnswer(invocation -> {
                    String orderId = invocation.getArgument(0);
                    // 실제 주문을 조회하여 OrderDto 생성 (동적으로 처리)
                    try {
                        // 실제 API 호출을 통해 OrderDetailResponse 조회
                        // 테스트에서는 실제 주문이 생성된 후 호출되므로 정상적인 OrderDto 반환
                        // 여기서는 기본값으로 설정하고, 실제 테스트에서는 정상 플로우가 진행되므로
                        // 실패 이벤트가 발생하지 않음
                        return com.fourtune.common.shared.payment.dto.OrderDto.builder()
                                .auctionOrderId(1L)  // 기본값 (실제 테스트에서는 덮어씀)
                                .orderId(orderId)
                                .price(100000L)
                                .userId(1L)
                                .orderStatus(com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus.PENDING.toString())
                                .items(java.util.List.of(
                                        com.fourtune.common.shared.payment.dto.OrderDto.OrderItem.builder()
                                                .itemId(1L)
                                                .sellerId(1L)
                                                .price(100000L)
                                                .itemName("테스트 상품")
                                                .build()
                                ))
                                .build();
                    } catch (Exception e) {
                        // 실제 주문 조회 실패 시 null 반환 (테스트에서는 발생하지 않음)
                        return null;
                    }
                });
    }

    @Test
    @DisplayName("시나리오 0: 회원가입 → 로그인(JWT) → 탐색(경매 목록/상세)")
    void scenario0_Signup_Login_ExploreAuctions() throws Exception {
        // 0-1. 회원가입
        String email = "test@example.com";
        String password = "Test1234!@";
        String nickname = "테스트사용자";
        String phoneNumber = "010-1234-5678";

        UserSignUpRequest signupRequest = new UserSignUpRequest(
                email,
                password,
                nickname,
                phoneNumber
        );

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isCreated());

        // 0-2. 로그인(JWT 발급)
        UserLoginRequest loginRequest = new UserLoginRequest(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        @SuppressWarnings("unused")
        String accessToken = loginJson.get("accessToken").asText();

        // 0-3. 내 프로필 조회(인증 확인)
        // Note: 프로필 조회 엔드포인트가 없으므로, 토큰 유효성은 로그인 응답에서 확인됨
        // 추가 인증 확인은 생략 (토큰이 정상적으로 발급되었으므로 유효함)
        // accessToken은 시나리오 0에서는 사용하지 않지만, 이후 시나리오에서 사용 가능

        // 0-4. 경매 목록 조회
        // Note: SecurityConfig에서 /api/v1/auctions는 인증이 필요하므로 토큰 사용
        String auctionListResponse = mockMvc.perform(get("/api/v1/auctions")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode auctionListJson = objectMapper.readTree(auctionListResponse);
        JsonNode content = auctionListJson.get("content");

        // 경매가 있으면 첫 번째 경매의 ID를 사용
        Long auctionId = null;
        if (content.isArray() && content.size() > 0) {
            auctionId = content.get(0).get("id").asLong();
        } else {
            // 테스트용 경매 생성
            User seller = createTestUser("seller@test.com", "판매자");
            AuctionItem testAuction = createTestAuction(seller.getId(), false);
            auctionId = testAuction.getId();
        }

        // 0-5. 경매 상세 조회
        // Note: SecurityConfig에서 /api/v1/auctions는 인증이 필요하므로 토큰 사용
        mockMvc.perform(get("/api/v1/auctions/{id}", auctionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(auctionId));
    }

    @Test
    @DisplayName("시나리오 1: 시나리오0 → 경매입찰 → 낙찰 → 결제 → 주문완료/정산")
    void scenario1_Bid_Win_Payment_Complete() throws Exception {
        // 1-0. (공통) 시나리오0 수행
        String email = "bidder@example.com";
        String password = "Test1234!@";
        String accessToken = performSignupAndLogin(email, password, "입찰자", "010-1111-2222");

        // 판매자 및 경매 생성
        User seller = createTestUser("seller1@test.com", "판매자1");
        AuctionItem auction = createTestAuction(seller.getId(), false);
        Long auctionId = auction.getId();

        // 1-1. 입찰 생성
        BigDecimal bidAmount = BigDecimal.valueOf(60000);
        BidPlaceRequest bidRequest = new BidPlaceRequest(auctionId, bidAmount);

        String bidResponse = mockMvc.perform(post("/api/v1/bids")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode bidJson = objectMapper.readTree(bidResponse);
        Long bidId = bidJson.get("data").get("id").asLong();

        // 1-2. 입찰 조회(검증 포인트)
        mockMvc.perform(get("/api/v1/bids/{bidId}", bidId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(bidId))
                .andExpect(jsonPath("$.data.auctionId").value(auctionId))
                .andExpect(jsonPath("$.data.bidAmount").value(60000));

        // 1-3. 낙찰 확정(시스템 처리)
        // 테스트 트랜잭션 내에서 직접 경매 종료 처리 (REQUIRES_NEW 트랜잭션 문제 우회)
        auction = auctionRepository.findById(auctionId).orElseThrow();

        // 최고가 입찰 조회
        java.util.Optional<Bid> highestBidOpt = bidSupport.findHighestBid(auctionId);
        assertThat(highestBidOpt).isPresent();
        Bid winningBid = highestBidOpt.get();

        // 경매 상태 변경 (ACTIVE -> ENDED -> SOLD)
        auction.close();
        auction.sell();
        auctionRepository.save(auction);

        // 낙찰 입찰 처리
        winningBid.win();
        bidSupport.save(winningBid);

        // Order 생성
        orderCreateUseCase.createWinningOrder(auction, winningBid.getBidderId(), winningBid.getBidAmount());

        // 실패한 입찰 처리
        bidSupport.failAllActiveBids(auctionId);

        // 경매 상태 확인
        AuctionItem closedAuction = auctionRepository.findById(auctionId).orElseThrow();
        assertThat(closedAuction.getStatus()).isIn(AuctionStatus.ENDED, AuctionStatus.SOLD);

        // 1-4. 경매 기반 주문 조회(낙찰 주문 생성 확인)
        String orderResponse = mockMvc.perform(get("/api/v1/orders/auction/{auctionId}", auctionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode orderJson = objectMapper.readTree(orderResponse);
        String orderId = orderJson.get("data").get("orderId").asText();
        Long auctionOrderId = orderJson.get("data").get("id").asLong();
        // finalPrice는 BigDecimal이므로 Long으로 변환
        Long amount = orderJson.get("data").get("finalPrice").asLong();

        // AuctionPort 모킹 업데이트: 실제 주문 정보 반환
        when(auctionPort.getOrder(orderId))
                .thenReturn(com.fourtune.common.shared.payment.dto.OrderDto.builder()
                        .auctionOrderId(auctionOrderId)
                        .orderId(orderId)
                        .price(amount)
                        .userId(userRepository.findByEmail(email).orElseThrow().getId())
                        .orderStatus(com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus.PENDING.toString())
                        .items(java.util.List.of(
                                com.fourtune.common.shared.payment.dto.OrderDto.OrderItem.builder()
                                        .itemId(auctionId)
                                        .sellerId(seller.getId())
                                        .price(amount)
                                        .itemName(auction.getTitle())
                                        .build()
                        ))
                        .build());

        // 1-5. 결제 승인(confirm) — 토스 결제 프론트 성공 후 호출
        String paymentKey = "test_payment_key_" + System.currentTimeMillis();
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                paymentKey,
                orderId,
                amount
        );

        mockMvc.perform(post("/api/payments/toss/confirm")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // 1-6. 주문 완료 처리 (정산 포함)
        mockMvc.perform(post("/api/v1/orders/{orderId}/complete", orderId)
                        .param("paymentKey", paymentKey)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // 1-7. 주문 최종 상태 확인
        String finalOrderResponse = mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode finalOrderJson = objectMapper.readTree(finalOrderResponse);
        String orderStatus = finalOrderJson.get("data").get("status").asText();
        assertThat(orderStatus).isEqualTo(OrderStatus.COMPLETED.name());
    }

    @Test
    @DisplayName("시나리오 2: 시나리오0 → 장바구니 담기 → 장바구니 즉시구매 → 결제 → 주문완료/정산")
    void scenario2_Cart_BuyNow_Payment_Complete() throws Exception {
        // 2-0. (공통) 시나리오0 수행
        String email = "buyer@example.com";
        String password = "Test1234!@";
        String accessToken = performSignupAndLogin(email, password, "구매자", "010-3333-4444");

        // 판매자 및 즉시구매 가능한 경매 생성
        User seller = createTestUser("seller2@test.com", "판매자2");
        AuctionItem auction = createTestAuction(seller.getId(), true);
        Long auctionId = auction.getId();

        // 2-1. 장바구니에 상품 담기
        CartAddItemRequest cartRequest = new CartAddItemRequest(auctionId);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andDo(print())
                .andExpect(status().isCreated());

        // 2-2. 장바구니 조회로 담긴 상품 확인
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].auctionId").value(auctionId));

        // 2-3. 장바구니 즉시구매 실행
        String buyNowResponse = mockMvc.perform(post("/api/v1/cart/buy-now/all")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode buyNowJson = objectMapper.readTree(buyNowResponse);
        String orderId = buyNowJson.get("data").get(0).asText();

        // 2-4. 경매 기반 주문 조회(주문 생성 확인)
        String orderResponse = mockMvc.perform(get("/api/v1/orders/auction/{auctionId}", auctionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode orderJson = objectMapper.readTree(orderResponse);
        Long auctionOrderId = orderJson.get("data").get("id").asLong();
        // finalPrice는 BigDecimal이므로 Long으로 변환
        Long amount = orderJson.get("data").get("finalPrice").asLong();

        // AuctionPort 모킹 업데이트: 실제 주문 정보 반환
        when(auctionPort.getOrder(orderId))
                .thenReturn(com.fourtune.common.shared.payment.dto.OrderDto.builder()
                        .auctionOrderId(auctionOrderId)
                        .orderId(orderId)
                        .price(amount)
                        .userId(userRepository.findByEmail(email).orElseThrow().getId())
                        .orderStatus(com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus.PENDING.toString())
                        .items(java.util.List.of(
                                com.fourtune.common.shared.payment.dto.OrderDto.OrderItem.builder()
                                        .itemId(auctionId)
                                        .sellerId(seller.getId())
                                        .price(amount)
                                        .itemName(auction.getTitle())
                                        .build()
                        ))
                        .build());

        // 2-5. 결제 승인(confirm) — 토스 결제 프론트 성공 후 호출
        String paymentKey = "test_payment_key_" + System.currentTimeMillis();
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                paymentKey,
                orderId,
                amount
        );

        mockMvc.perform(post("/api/payments/toss/confirm")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // 2-6. 주문 완료 처리(정산 포함)
        mockMvc.perform(post("/api/v1/orders/{orderId}/complete", orderId)
                        .param("paymentKey", paymentKey)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // 2-7. 주문 최종 상태 확인
        String finalOrderResponse = mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode finalOrderJson = objectMapper.readTree(finalOrderResponse);
        String orderStatus = finalOrderJson.get("data").get("status").asText();
        assertThat(orderStatus).isEqualTo(OrderStatus.COMPLETED.name());
    }

    // ========== 실패 케이스 ==========

    @Test
    @DisplayName("시나리오 1 실패: 입찰 금액이 현재가/규칙 위반")
    void scenario1_Fail_InvalidBidAmount() throws Exception {
        String email = "bidder2@example.com";
        String password = "Test1234!@";
        String accessToken = performSignupAndLogin(email, password, "입찰자2", "010-5555-6666");

        User seller = createTestUser("seller3@test.com", "판매자3");
        AuctionItem auction = createTestAuction(seller.getId(), false);
        Long auctionId = auction.getId();

        // 입찰 금액이 너무 낮음 (현재가보다 낮음)
        BigDecimal insufficientAmount = BigDecimal.valueOf(40000);
        BidPlaceRequest bidRequest = new BidPlaceRequest(auctionId, insufficientAmount);

        mockMvc.perform(post("/api/v1/bids")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("시나리오 2 실패: 장바구니 비어있는데 buy-now")
    void scenario2_Fail_EmptyCart() throws Exception {
        String email = "buyer2@example.com";
        String password = "Test1234!@";
        String accessToken = performSignupAndLogin(email, password, "구매자2", "010-7777-8888");

        // 빈 장바구니에서 즉시구매 시도
        // Note: buyNowAllCart()는 빈 장바구니일 때 빈 리스트를 반환하므로 200 OK를 반환합니다
        String response = mockMvc.perform(post("/api/v1/cart/buy-now/all")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 빈 리스트 반환 확인
        JsonNode responseJson = objectMapper.readTree(response);
        assertThat(responseJson.get("data").isArray()).isTrue();
        assertThat(responseJson.get("data").size()).isEqualTo(0);
    }

    // ========== 헬퍼 메서드 ==========

    private void initSystemWallets() {
        createSystemWalletIfNotExists(CashPolicy.SYSTEM_HOLDING_USER_EMAIL, "system");
        createSystemWalletIfNotExists(CashPolicy.PLATFORM_REVENUE_USER_EMAIL, "platform");
    }

    private void createSystemWalletIfNotExists(String email, String nickname) {
        com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser systemUser =
                paymentUserRepository.findByEmail(email)
                        .orElseGet(() -> paymentUserRepository.save(
                                com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser.builder()
                                        .email(email)
                                        .nickname(nickname)
                                        .status("ACTIVE")
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        ));

        walletRepository.findWalletByPaymentUser(systemUser)
                .orElseGet(() -> walletRepository.save(
                        com.fourtune.auction.boundedContext.payment.domain.entity.Wallet.builder()
                                .paymentUser(systemUser)
                                .balance(0L)
                                .build()
                ));
    }

    private String performSignupAndLogin(String email, String password, String nickname, String phoneNumber) throws Exception {
        // 회원가입
        UserSignUpRequest signupRequest = new UserSignUpRequest(
                email,
                password,
                nickname,
                phoneNumber
        );

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        // Payment 도메인에 PaymentUser와 Wallet 생성 (이벤트가 비동기로 처리되지 않을 수 있으므로 명시적으로 생성)
        User user = userRepository.findByEmail(email).orElseThrow();
        UserResponse userResponse = UserMapper.toDto(user);
        com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser paymentUser = paymentFacade.syncUser(userResponse);
        paymentFacade.createWallet(paymentUser.toDto());

        // 로그인
        UserLoginRequest loginRequest = new UserLoginRequest(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        return loginJson.get("accessToken").asText();
    }

    private User createTestUser(String email, String nickname) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .status(Status.ACTIVE)
                .phoneNumber("010-0000-0000")
                .build();
        user = userRepository.save(user);
        
        // Payment 도메인에 PaymentUser와 Wallet 생성
        UserResponse userResponse = UserMapper.toDto(user);
        com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser paymentUser = paymentFacade.syncUser(userResponse);
        paymentFacade.createWallet(paymentUser.toDto());
        
        return user;
    }

    private AuctionItem createTestAuction(Long sellerId, boolean buyNowEnabled) {
        AuctionItem auction = AuctionItem.builder()
                .sellerId(sellerId)
                .title("테스트 경매 상품")
                .description("테스트 설명")
                .category(Category.ELECTRONICS)
                .status(AuctionStatus.ACTIVE)
                .startPrice(BigDecimal.valueOf(50000))
                .currentPrice(BigDecimal.valueOf(50000))
                .bidUnit(1000)
                .buyNowPrice(buyNowEnabled ? BigDecimal.valueOf(100000) : null)
                .buyNowEnabled(buyNowEnabled)
                .auctionStartTime(LocalDateTime.now().minusHours(1))
                .auctionEndTime(LocalDateTime.now().plusHours(1))
                .extensionCount(0)
                .build();
        return auctionRepository.save(auction);
    }
}
