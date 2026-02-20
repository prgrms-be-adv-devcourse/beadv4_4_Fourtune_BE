package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import com.fourtune.auction.boundedContext.auction.port.out.BidRepository;
import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.mapper.UserMapper;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.common.global.security.jwt.JwtTokenProvider;
import com.fourtune.common.shared.user.dto.UserResponse;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import com.fourtune.common.shared.auction.dto.BidPlaceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktbWluLTI1Ni1iaXRzLXJlcXVpcmVkLWZvci1obWFjLXNoYTI1Ni1hbGdvcml0aG0tdGVzdC1rZXktMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=",
    "jwt.access-token-expiration=3600000",
    "jwt.refresh-token-expiration=1209600000",
    "spring.data.elasticsearch.repositories.enabled=false"
})
class ApiV1BidControllerIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuctionItemRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

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

    @MockitoBean
    private com.fourtune.common.shared.watchList.kafka.WatchListKafkaProducer watchListKafkaProducer;

    @MockitoBean
    private com.fourtune.common.shared.notification.kafka.NotificationKafkaProducer notificationKafkaProducer;

    @MockitoBean
    private com.fourtune.common.shared.search.kafka.SearchKafkaProducer searchKafkaProducer;

    private AuctionItem activeAuction;
    private User seller;
    private User bidder;
    private String bidderToken;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
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

        bidder = User.builder()
                .email("bidder@test.com")
                .nickname("입찰자")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        bidder = userRepository.save(bidder);

        UserResponse bidderDto = UserMapper.toDto(bidder);
        // JWT 토큰 생성
        bidderToken = jwtTokenProvider.createAccessToken(bidderDto);

        // 테스트용 ACTIVE 상태 경매 생성
        activeAuction = AuctionItem.builder()
                .sellerId(seller.getId())
                .title("테스트 경매")
                .description("테스트 설명")
                .category(Category.ELECTRONICS)
                .status(AuctionStatus.ACTIVE)
                .startPrice(BigDecimal.valueOf(50000))
                .currentPrice(BigDecimal.valueOf(50000))
                .bidUnit(1000)
                .auctionStartTime(LocalDateTime.now().minusHours(1))
                .auctionEndTime(LocalDateTime.now().plusHours(1))
                .extensionCount(0)
                .build();
        activeAuction = auctionRepository.save(activeAuction);
    }

    @Test
    @DisplayName("TC-1-1: 정상 입찰 성공")
    void testPlaceBid_Success() throws Exception {
        // Given
        BigDecimal bidAmount = BigDecimal.valueOf(60000);
        BidPlaceRequest request = new BidPlaceRequest(
                activeAuction.getId(),
                bidAmount
        );

        // When & Then
        mockMvc.perform(post("/api/v1/bids")
                        .header("Authorization", "Bearer " + bidderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.bidAmount").value(60000));

        // DB 검증
        Bid savedBid = bidRepository.findByAuctionIdOrderByCreatedAtDesc(activeAuction.getId())
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(savedBid.getBidAmount()).isEqualByComparingTo(bidAmount);
        assertThat(savedBid.getBidderId()).isEqualTo(bidder.getId());
        assertThat(savedBid.getStatus()).isEqualTo(BidStatus.ACTIVE);

        // 경매 업데이트 검증
        AuctionItem updatedAuction = auctionRepository.findById(activeAuction.getId())
                .orElseThrow();
        assertThat(updatedAuction.getCurrentPrice()).isEqualByComparingTo(bidAmount);
        assertThat(updatedAuction.getBidCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("TC-1-2: 입찰 시 자동 연장 발생")
    void testPlaceBid_WithAutoExtension() throws Exception {
        // Given: 종료 시간 5분 이내로 설정
        activeAuction = AuctionItem.builder()
                .sellerId(seller.getId())
                .title("테스트 경매")
                .description("테스트 설명")
                .category(Category.ELECTRONICS)
                .status(AuctionStatus.ACTIVE)
                .startPrice(BigDecimal.valueOf(50000))
                .currentPrice(BigDecimal.valueOf(50000))
                .bidUnit(1000)
                .auctionStartTime(LocalDateTime.now().minusHours(1))
                .auctionEndTime(LocalDateTime.now().plusMinutes(4)) // 5분 이내
                .extensionCount(0)
                .build();
        activeAuction = auctionRepository.save(activeAuction);

        LocalDateTime originalEndTime = activeAuction.getAuctionEndTime();
        BigDecimal bidAmount = BigDecimal.valueOf(60000);

        BidPlaceRequest request = new BidPlaceRequest(
                activeAuction.getId(),
                bidAmount
        );

        // When
        mockMvc.perform(post("/api/v1/bids")
                        .header("Authorization", "Bearer " + bidderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then: 종료 시간 3분 연장 확인
        AuctionItem updatedAuction = auctionRepository.findById(activeAuction.getId())
                .orElseThrow();

        assertThat(updatedAuction.getAuctionEndTime())
                .isAfter(originalEndTime);
        assertThat(updatedAuction.getExtensionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("TC-1-3: 경매 상태 오류로 입찰 실패")
    void testPlaceBid_Fail_InvalidStatus() throws Exception {
        // Given: 경매 상태를 SCHEDULED로 변경
        activeAuction = AuctionItem.builder()
                .sellerId(seller.getId())
                .title("테스트 경매")
                .description("테스트 설명")
                .category(Category.ELECTRONICS)
                .status(AuctionStatus.SCHEDULED)
                .startPrice(BigDecimal.valueOf(50000))
                .currentPrice(BigDecimal.valueOf(50000))
                .bidUnit(1000)
                .auctionStartTime(LocalDateTime.now().plusHours(1))
                .auctionEndTime(LocalDateTime.now().plusHours(2))
                .build();
        activeAuction = auctionRepository.save(activeAuction);

        BidPlaceRequest request = new BidPlaceRequest(
                activeAuction.getId(),
                BigDecimal.valueOf(60000)
        );

        // When & Then
        mockMvc.perform(post("/api/v1/bids")
                        .header("Authorization", "Bearer " + bidderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // DB 검증: 입찰 생성되지 않음
        long bidCount = bidRepository.count();
        assertThat(bidCount).isEqualTo(0);
    }

    @Test
    @DisplayName("TC-1-4: 입찰 금액 부족으로 실패")
    void testPlaceBid_Fail_AmountTooLow() throws Exception {
        // Given: 현재가 50,000원, 입찰단위 1,000원
        // 입찰 금액: 50,500원 (최소 51,000원 필요)
        BigDecimal insufficientAmount = BigDecimal.valueOf(50500);

        BidPlaceRequest request = new BidPlaceRequest(
                activeAuction.getId(),
                insufficientAmount
        );

        // When & Then - 인증이 필요하므로 인증 토큰 포함
        mockMvc.perform(post("/api/v1/bids")
                        .header("Authorization", "Bearer " + bidderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // DB 검증
        assertThat(bidRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("TC-8-1: 인증 없이 입찰 시도")
    void testPlaceBid_Unauthorized() throws Exception {
        // Given
        BidPlaceRequest request = new BidPlaceRequest(
                activeAuction.getId(),
                BigDecimal.valueOf(60000)
        );

        // When & Then - 인증 없이 요청 시 401, 403 또는 302(리다이렉트) 반환 가능
        mockMvc.perform(post("/api/v1/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Spring Security는 인증이 없을 때 상황에 따라 다른 상태 코드를 반환할 수 있음
                    // 401: Unauthorized (인증 필요)
                    // 403: Forbidden (권한 없음)
                    // 302: Redirect (OAuth2 로그인 페이지로 리다이렉트)
                    assertThat(status).isIn(401, 403, 302);
                });

        // DB 검증
        assertThat(bidRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("TC-8-2: 판매자 본인 입찰 시도")
    void testPlaceBid_SelfAuction() throws Exception {
        // Given
        UserResponse sellerDto = UserMapper.toDto(seller);

        String sellerToken = jwtTokenProvider.createAccessToken(sellerDto);
        BidPlaceRequest request = new BidPlaceRequest(
                activeAuction.getId(),
                BigDecimal.valueOf(60000)
        );

        // When & Then
        mockMvc.perform(post("/api/v1/bids")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // DB 검증
        assertThat(bidRepository.count()).isEqualTo(0);
    }
}
