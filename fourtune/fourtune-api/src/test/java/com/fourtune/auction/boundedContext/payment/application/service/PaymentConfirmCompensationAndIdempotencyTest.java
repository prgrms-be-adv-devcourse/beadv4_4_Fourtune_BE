package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentRepository;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentGatewayPort;
import com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.shared.payment.constant.CashPolicy;
import com.fourtune.shared.payment.dto.OrderDto;
import com.fourtune.shared.auction.dto.OrderDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PAY-05: 보상 트랜잭션 (PG 승인 후 내부 실패 시 cancel 호출 검증)
 * PAY-03: 결제 멱등성 (동일 orderId 재요청 시 기존 결과 반환, 중복 Payment/지갑 변동 없음)
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
                "spring.data.elasticsearch.repositories.enabled=false",
                "feature.kafka.enabled=false"
})
class PaymentConfirmCompensationAndIdempotencyTest {

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
        private PaymentFacade paymentFacade;
        @Autowired
        private PaymentUserRepository paymentUserRepository;
        @Autowired
        private WalletRepository walletRepository;
        @Autowired
        private PaymentRepository paymentRepository;

        private PaymentUser systemUser;
        private PaymentUser platformUser;
        private PaymentUser customerUser;
        private static final String ORDER_ID = "e2e-order-uuid-001";
        private static final String PAYMENT_KEY = "e2e-payment-key-001";
        private static final Long ORDER_AMOUNT = 15_000L;
        private static final Long AUCTION_ORDER_ID = 200L;

        @BeforeEach
        void setUp() {
                LocalDateTime now = LocalDateTime.now();
                systemUser = paymentUserRepository.findByEmail(CashPolicy.SYSTEM_HOLDING_USER_EMAIL)
                                .orElseGet(() -> paymentUserRepository.save(
                                                PaymentUser.builder()
                                                                .id(1L)
                                                                .email(CashPolicy.SYSTEM_HOLDING_USER_EMAIL)
                                                                .nickname("system-holding")
                                                                .password("")
                                                                .phoneNumber("")
                                                                .createdAt(now)
                                                                .updatedAt(now)
                                                                .deletedAt(null)
                                                                .status("ACTIVE")
                                                                .build()));
                platformUser = paymentUserRepository.findByEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                                .orElseGet(() -> paymentUserRepository.save(
                                                PaymentUser.builder()
                                                                .id(2L)
                                                                .email(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                                                                .nickname("platform-revenue")
                                                                .password("")
                                                                .phoneNumber("")
                                                                .createdAt(now)
                                                                .updatedAt(now)
                                                                .deletedAt(null)
                                                                .status("ACTIVE")
                                                                .build()));
                customerUser = paymentUserRepository.findByEmail("idempotency-test@test.com")
                                .orElseGet(() -> paymentUserRepository.save(
                                                PaymentUser.builder()
                                                                .id(101L)
                                                                .email("idempotency-test@test.com")
                                                                .nickname("customer")
                                                                .password("")
                                                                .phoneNumber("")
                                                                .createdAt(now)
                                                                .updatedAt(now)
                                                                .deletedAt(null)
                                                                .status("ACTIVE")
                                                                .build()));

                walletRepository.findWalletByPaymentUser(systemUser)
                                .orElseGet(() -> walletRepository
                                                .save(Wallet.builder().paymentUser(systemUser).balance(0L).build()));
                walletRepository.findWalletByPaymentUser(platformUser)
                                .orElseGet(() -> walletRepository
                                                .save(Wallet.builder().paymentUser(platformUser).balance(0L).build()));
                walletRepository.findWalletByPaymentUser(customerUser)
                                .orElseGet(() -> walletRepository
                                                .save(Wallet.builder().paymentUser(customerUser).balance(0L).build()));

                paymentRepository.deleteAll();
        }

        private OrderDetailResponse orderDetailResponse(Long userId, Long price) {
                return new OrderDetailResponse(
                                AUCTION_ORDER_ID, // id
                                ORDER_ID, // orderId
                                1L, // auctionId
                                "테스트 상품", // auctionTitle
                                null, // thumbnailUrl
                                userId, // winnerId
                                "닉네임", // winnerNickname
                                2L, // sellerId
                                "판매자", // sellerNickname
                                BigDecimal.valueOf(price), // finalPrice
                                "BUY_NOW", // orderType
                                "PENDING", // status
                                null, // paymentKey
                                null, // paidAt
                                LocalDateTime.now() // createdAt
                );
        }

    @Test
    @DisplayName("[PAY-05] PG 승인 성공 후 내부 로직(DB) 실패 시 cancel()이 1회 호출되어야 한다 (보상 트랜잭션)")
    void confirmPayment_whenInternalLogicFails_callsCancelOnce() {
        when(paymentGatewayPort.confirm(eq(PAYMENT_KEY), eq(ORDER_ID), eq(ORDER_AMOUNT)))
                .thenReturn(PaymentExecutionResult.success(PAYMENT_KEY, ORDER_ID, ORDER_AMOUNT));
        when(auctionPort.getOrder(eq(ORDER_ID)))
                .thenThrow(new RuntimeException("Simulated DB failure"));

        assertThatThrownBy(() ->
                paymentFacade.confirmPayment(PAYMENT_KEY, ORDER_ID, ORDER_AMOUNT, customerUser.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated DB failure");

        verify(paymentGatewayPort, times(1)).confirm(eq(PAYMENT_KEY), eq(ORDER_ID), eq(ORDER_AMOUNT));
        verify(paymentGatewayPort, times(1)).cancel(eq(PAYMENT_KEY), startsWith("System Logic Failed:"), eq(null));
    }

        @Test
        @DisplayName("[PAY-03] 동일 orderId로 두 번째 결제 요청 시 멱등: Payment 1건, 지갑 추가 차감 없음")
        void confirmPayment_idempotency_sameOrderIdReturnsExistingNoDuplicatePayment() {
                OrderDetailResponse orderResponse = orderDetailResponse(customerUser.getId(), ORDER_AMOUNT);
                when(paymentGatewayPort.confirm(eq(PAYMENT_KEY), eq(ORDER_ID), eq(ORDER_AMOUNT)))
                                .thenReturn(PaymentExecutionResult.success(PAYMENT_KEY, ORDER_ID, ORDER_AMOUNT));
                when(auctionPort.getOrder(eq(ORDER_ID)))
                                .thenReturn(OrderDto.from(orderResponse));

                PaymentExecutionResult first = paymentFacade.confirmPayment(PAYMENT_KEY, ORDER_ID, ORDER_AMOUNT,
                                customerUser.getId());
                assertThat(first.isSuccess()).isTrue();

                List<Payment> afterFirst = paymentRepository.findPaymentsByPaymentUserId(customerUser.getId());
                assertThat(afterFirst).hasSize(1);
                Long balanceAfterFirst = paymentFacade.getBalance(customerUser.getId());
                assertThat(balanceAfterFirst).isZero();

                PaymentExecutionResult second = paymentFacade.confirmPayment(PAYMENT_KEY, ORDER_ID, ORDER_AMOUNT,
                                customerUser.getId());
                assertThat(second.isSuccess()).isTrue();
                assertThat(second.getOrderId()).isEqualTo(ORDER_ID);
                assertThat(second.getAmount()).isEqualTo(ORDER_AMOUNT);

                List<Payment> afterSecond = paymentRepository.findPaymentsByPaymentUserId(customerUser.getId());
                assertThat(afterSecond).hasSize(1);
                Long balanceAfterSecond = paymentFacade.getBalance(customerUser.getId());
                assertThat(balanceAfterSecond).isZero();

                verify(paymentGatewayPort, times(1)).confirm(anyString(), anyString(), anyLong());
        }
}
