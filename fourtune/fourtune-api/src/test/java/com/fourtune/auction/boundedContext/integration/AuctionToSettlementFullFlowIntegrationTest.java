package com.fourtune.auction.boundedContext.integration;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentConfirmUseCase;
import com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType;
import com.fourtune.auction.boundedContext.payment.domain.constant.PaymentStatus;
import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentGatewayPort;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentRepository;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.shared.payment.constant.CashPolicy;
import com.fourtune.shared.payment.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 경매 사이트 전체 플로우 통합 테스트 (주문 → 결제 → 정산)
 * - 시나리오: 유저가 낙찰/주문 후 결제 완료 → 정산 후보 등록 → 정산 완료(시스템→플랫폼/판매자 지갑)
 * - 경매·입찰·주문 생성은 auction-service 담당이므로, 여기서는 주문(OrderDto)을 스텁하여 결제·정산만 검증
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.data.elasticsearch.repositories.enabled=false",
        "feature.kafka.enabled=false"
})
class AuctionToSettlementFullFlowIntegrationTest {

    @MockitoBean
    private com.google.firebase.messaging.FirebaseMessaging firebaseMessaging;
    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.in.event.AuctionItemIndexEventListener auctionItemIndexEventListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.ElasticsearchAuctionItemIndexingHandler elasticsearchAuctionItemIndexingHandler;
    @MockitoBean
    private com.fourtune.auction.boundedContext.payment.adapter.in.kafka.PaymentKafkaListener paymentKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.payment.adapter.in.kafka.PaymentUserKafkaListener paymentUserKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.payment.adapter.in.kafka.PaymentSettlementKafkaListener paymentSettlementKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.watchList.adapter.in.kafka.WatchListUserKafkaListener watchListUserKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.settlement.adapter.in.kafka.SettlementUserKafkaListener settlementUserKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.notification.adapter.in.kafka.NotificationUserKafkaListener notificationUserKafkaListener;

    @MockitoBean
    private PaymentGatewayPort paymentGatewayPort;
    @MockitoBean
    private AuctionPort auctionPort;

    @Autowired
    private PaymentConfirmUseCase paymentConfirmUseCase;
    @Autowired
    private PaymentUserRepository paymentUserRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    private static final String ORDER_ID = "full-flow-order-001";
    private static final Long AUCTION_ORDER_ID = 200L;
    private static final long ORDER_AMOUNT = 10_000L;
    private static final String PAYMENT_KEY = "test-pg-key-001";

    private PaymentUser systemUser;
    private PaymentUser platformUser;
    private PaymentUser buyerUser;
    private PaymentUser sellerUser;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        systemUser = paymentUserRepository.findByEmail(CashPolicy.SYSTEM_HOLDING_USER_EMAIL)
                .orElseGet(() -> paymentUserRepository.save(PaymentUser.builder()
                        .id(900L)
                        .email(CashPolicy.SYSTEM_HOLDING_USER_EMAIL)
                        .nickname("system-holding")
                        .password("").phoneNumber("")
                        .createdAt(now).updatedAt(now).deletedAt(null).status("ACTIVE")
                        .build()));
        platformUser = paymentUserRepository.findByEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                .orElseGet(() -> paymentUserRepository.save(PaymentUser.builder()
                        .id(901L)
                        .email(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                        .nickname("platform-revenue")
                        .password("").phoneNumber("")
                        .createdAt(now).updatedAt(now).deletedAt(null).status("ACTIVE")
                        .build()));
        buyerUser = paymentUserRepository.findByEmail("buyer-fullflow@test.com")
                .orElseGet(() -> paymentUserRepository.save(PaymentUser.builder()
                        .id(100L)
                        .email("buyer-fullflow@test.com")
                        .nickname("buyer_user")
                        .password("").phoneNumber("")
                        .createdAt(now).updatedAt(now).deletedAt(null).status("ACTIVE")
                        .build()));
        sellerUser = paymentUserRepository.findByEmail("seller@test.com")
                .orElseGet(() -> paymentUserRepository.save(PaymentUser.builder()
                        .id(101L)
                        .email("seller@test.com")
                        .nickname("seller_user")
                        .password("").phoneNumber("")
                        .createdAt(now).updatedAt(now).deletedAt(null).status("ACTIVE")
                        .build()));

        // 지갑: 구매자 잔액 충분, 시스템/플랫폼/판매자 지갑
        ensureWallet(buyerUser, ORDER_AMOUNT);
        ensureWallet(systemUser, 0L);
        ensureWallet(platformUser, 0L);
        ensureWallet(sellerUser, 0L);

        when(paymentGatewayPort.confirm(anyString(), anyString(), anyLong()))
                .thenReturn(PaymentExecutionResult.success(PAYMENT_KEY, ORDER_ID, ORDER_AMOUNT));

        OrderDto orderDto = orderDto(buyerUser.getId(), sellerUser.getId(), ORDER_AMOUNT);
        when(auctionPort.getOrder(eq(ORDER_ID))).thenReturn(orderDto);
    }

    private void ensureWallet(PaymentUser user, long initialBalance) {
        Wallet w = walletRepository.findWalletByPaymentUser(user).orElse(null);
        if (w == null) {
            w = Wallet.builder().paymentUser(user).balance(0L).build();
            walletRepository.save(w);
        }
        if (initialBalance > 0 && w.getBalance() < initialBalance) {
            w.credit(initialBalance - w.getBalance(), CashEventType.충전__PG결제_토스페이먼츠, "test", 1L);
            walletRepository.saveAndFlush(w);
        }
    }

    private OrderDto orderDto(Long buyerId, Long sellerId, long price) {
        return OrderDto.builder()
                .orderId(ORDER_ID)
                .auctionOrderId(AUCTION_ORDER_ID)
                .price(price)
                .userId(buyerId)
                .orderStatus("PENDING")
                .items(List.of(
                        OrderDto.OrderItem.builder()
                                .itemId(AUCTION_ORDER_ID)
                                .sellerId(sellerId)
                                .price(price)
                                .itemName("테스트 상품")
                                .build()))
                .build();
    }

    @Test
    @DisplayName("[전체 플로우] 주문(스텁) → 결제 승인 → 결제 완료 → 지갑/결제 상태 검증")
    void fullFlow_orderToPayment_success() {
        // 결제 승인 (AuctionPort 스텁으로 주문 반환 → 내부에서 주문 검증 + cashComplete 수행)
        paymentConfirmUseCase.confirmPayment(PAYMENT_KEY, ORDER_ID, ORDER_AMOUNT, buyerUser.getId());
        walletRepository.flush();

        // 검증: 결제 APPROVED, 고객 지갑은 PG충전(pgAmount) 후 주문금액 차감 → 원래 잔액 유지, 시스템 지갑 입금
        List<Payment> payments = paymentRepository.findPaymentsByPaymentUserId(buyerUser.getId());
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payments.get(0).getOrderId()).isEqualTo(ORDER_ID);

        Wallet buyerWallet = walletRepository.findWalletByPaymentUser(buyerUser).orElseThrow();
        Wallet systemW = walletRepository.findWalletByPaymentUser(systemUser).orElseThrow();
        assertThat(buyerWallet.getBalance()).isEqualTo(ORDER_AMOUNT); // PG충전 후 주문 차감으로 초기 잔액과 동일
        assertThat(systemW.getBalance()).isEqualTo(ORDER_AMOUNT);
    }
}
