package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.constant.PaymentStatus;
import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentRepository;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.RefundRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.payment.constant.CashPolicy;
import com.fourtune.shared.payment.dto.OrderDto;
import com.fourtune.shared.settlement.dto.SettlementDto;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 결제·정산 도메인 통합 테스트
 * - 결제 완료(cashComplete): 고객 지갑 차감, 시스템 지갑 입금, Payment 저장
 * - 정산 완료(settlementCashComplete): 시스템 지갑 → 플랫폼/판매자 지갑 이체
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
                "spring.data.elasticsearch.repositories.enabled=false",
                "feature.kafka.enabled=false"
})
class PaymentAndSettlementIntegrationTest {

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

        @Autowired
        private PaymentCashCompleteUseCase paymentCashCompleteUseCase;
        @Autowired
        private PaymentCompleteSettlementUseCase paymentCompleteSettlementUseCase;
        @Autowired
        private PaymentFacade paymentFacade;
        @Autowired
        private PaymentUserRepository paymentUserRepository;
        @Autowired
        private WalletRepository walletRepository;
        @Autowired
        private PaymentRepository paymentRepository;
        @Autowired
        private RefundRepository refundRepository;

        private PaymentUser systemUser;
        private PaymentUser platformUser;
        private PaymentUser customerUser;
        private Wallet systemWallet;

        private static final long ORDER_AMOUNT = 10_000L;
        private static final String ORDER_ID = "test-order-uuid-001";
        private static final Long AUCTION_ORDER_ID = 100L;
        private static final String PAYMENT_KEY = "test-payment-key-001";

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
                customerUser = paymentUserRepository.findByEmail("customer-payment-test@test.com")
                                .orElseGet(() -> paymentUserRepository.save(
                                                PaymentUser.builder()
                                                                .id(100L)
                                                                .email("customer-payment-test@test.com")
                                                                .nickname("customer")
                                                                .password("")
                                                                .phoneNumber("")
                                                                .createdAt(now)
                                                                .updatedAt(now)
                                                                .deletedAt(null)
                                                                .status("ACTIVE")
                                                                .build()));

                systemWallet = walletRepository.findWalletByPaymentUser(systemUser)
                                .orElseGet(() -> walletRepository.save(
                                                Wallet.builder().paymentUser(systemUser).balance(0L).build()));
                walletRepository.findWalletByPaymentUser(platformUser)
                                .orElseGet(() -> walletRepository.save(
                                                Wallet.builder().paymentUser(platformUser).balance(0L).build()));
                walletRepository.findWalletByPaymentUser(customerUser)
                                .orElseGet(() -> walletRepository.save(
                                                Wallet.builder().paymentUser(customerUser).balance(0L).build()));

                paymentRepository.deleteAll();
                refundRepository.deleteAll();
        }

        private OrderDto orderDto(long userId, long price, String orderId, Long auctionOrderId) {
                return OrderDto.builder()
                                .userId(userId)
                                .price(price)
                                .orderId(orderId)
                                .auctionOrderId(auctionOrderId != null ? auctionOrderId : AUCTION_ORDER_ID)
                                .items(List.of(
                                                OrderDto.OrderItem.builder()
                                                                .itemId(1L)
                                                                .sellerId(2L)
                                                                .price(price)
                                                                .itemName("테스트 상품")
                                                                .build()))
                                .orderStatus("PENDING")
                                .build();
        }

        @Test
        @DisplayName("결제 완료 시 PG 충전 후 주문 금액 차감 → 고객 지갑 0, 시스템 지갑 증가, Payment APPROVED 저장")
        void cashComplete_success() {
                OrderDto order = orderDto(customerUser.getId(), ORDER_AMOUNT, ORDER_ID, AUCTION_ORDER_ID);
                long pgAmount = ORDER_AMOUNT;

                paymentCashCompleteUseCase.cashComplete(order, pgAmount, PAYMENT_KEY);

                Wallet customer = walletRepository.findWalletByPaymentUser(customerUser).orElseThrow();
                Wallet system = walletRepository.findWalletByPaymentUser(systemUser).orElseThrow();

                assertThat(customer.getBalance()).isZero();
                assertThat(system.getBalance()).isEqualTo(ORDER_AMOUNT);

                List<Payment> payments = paymentRepository.findPaymentsByPaymentUserId(customerUser.getId());
                assertThat(payments).hasSize(1);
                Payment payment = payments.get(0);
                assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
                assertThat(payment.getOrderId()).isEqualTo(ORDER_ID);
                assertThat(payment.getAmount()).isEqualTo(ORDER_AMOUNT);
                assertThat(payment.getBalanceAmount()).isEqualTo(ORDER_AMOUNT);
        }

        @Test
        @DisplayName("결제 시 고객 지갑 잔액 부족이면 PAYMENT_WALLET_INSUFFICIENT_BALANCE 예외")
        void cashComplete_insufficientBalance_throws() {
                OrderDto order = orderDto(customerUser.getId(), ORDER_AMOUNT, ORDER_ID, AUCTION_ORDER_ID);
                long pgAmount = 0L; // PG 충전 없이 결제만 시도 → 잔액 0으로 불가

                assertThatThrownBy(() -> paymentCashCompleteUseCase.cashComplete(order, pgAmount, PAYMENT_KEY))
                                .isInstanceOf(BusinessException.class)
                                .satisfies(ex -> {
                                        BusinessException be = (BusinessException) ex;
                                        assertThat(be.getErrorCode())
                                                        .isEqualTo(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
                                });

                List<Payment> payments = paymentRepository.findPaymentsByPaymentUserId(customerUser.getId());
                assertThat(payments).isEmpty();
        }

        @Test
        @DisplayName("정산 완료(플랫폼 수수료) 시 시스템 지갑 감소, 플랫폼 지갑 증가")
        void settlementCashComplete_platformFee() {
                systemWallet.credit(5_000L,
                                com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType.임시보관__주문결제,
                                "Order", 1L);
                walletRepository.save(systemWallet);

                long settleAmount = 3_000L;
                SettlementDto dto = SettlementDto.builder()
                                .id(1L)
                                .payeeId(platformUser.getId())
                                .payeeEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                                .amount(settleAmount)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .settledAt(LocalDateTime.now())
                                .build();

                Wallet result = paymentCompleteSettlementUseCase.settlementCashComplete(dto);

                Wallet system = walletRepository.findWalletByPaymentUser(systemUser).orElseThrow();
                Wallet platform = walletRepository.findWalletByPaymentUser(platformUser).orElseThrow();

                assertThat(system.getBalance()).isEqualTo(5_000L - settleAmount);
                assertThat(platform.getBalance()).isEqualTo(settleAmount);
                assertThat(result.getBalance()).isEqualTo(settleAmount);
        }

        @Test
        @DisplayName("정산 완료(판매자 대금) 시 시스템 지갑 감소, 판매자 지갑 증가")
        void settlementCashComplete_sellerPayout() {
                systemWallet.credit(10_000L,
                                com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType.임시보관__주문결제,
                                "Order", 1L);
                walletRepository.save(systemWallet);

                long settleAmount = 9_000L;
                SettlementDto dto = SettlementDto.builder()
                                .id(2L)
                                .payeeId(customerUser.getId())
                                .payeeEmail(customerUser.getEmail())
                                .amount(settleAmount)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .settledAt(LocalDateTime.now())
                                .build();

                paymentCompleteSettlementUseCase.settlementCashComplete(dto);

                Wallet system = walletRepository.findWalletByPaymentUser(systemUser).orElseThrow();
                Wallet payee = walletRepository.findWalletByPaymentUser(customerUser).orElseThrow();

                assertThat(system.getBalance()).isEqualTo(10_000L - settleAmount);
                assertThat(payee.getBalance()).isEqualTo(settleAmount);
        }

        @Test
        @DisplayName("정산 시 시스템 지갑 잔액 부족이면 PAYMENT_WALLET_INSUFFICIENT_BALANCE 예외")
        void settlementCashComplete_insufficientSystemBalance_throws() {
                long settleAmount = 5_000L;
                SettlementDto dto = SettlementDto.builder()
                                .id(3L)
                                .payeeId(platformUser.getId())
                                .payeeEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                                .amount(settleAmount)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .settledAt(LocalDateTime.now())
                                .build();

                assertThatThrownBy(() -> paymentCompleteSettlementUseCase.settlementCashComplete(dto))
                                .isInstanceOf(BusinessException.class)
                                .satisfies(ex -> {
                                        BusinessException be = (BusinessException) ex;
                                        assertThat(be.getErrorCode())
                                                        .isEqualTo(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
                                });
        }

        @Test
        @DisplayName("PaymentFacade: 결제 완료 후 지갑 잔액·결제 내역 조회 일치")
        void paymentFacade_afterCashComplete_balanceAndPaymentListMatch() {
                OrderDto order = orderDto(customerUser.getId(), ORDER_AMOUNT, ORDER_ID, AUCTION_ORDER_ID);
                paymentCashCompleteUseCase.cashComplete(order, ORDER_AMOUNT, PAYMENT_KEY);

                Long balance = paymentFacade.getBalance(customerUser.getId());
                List<Payment> list = paymentFacade.findPaymentListByUserId(customerUser.getId());

                assertThat(balance).isZero();
                assertThat(list).hasSize(1);
                assertThat(list.get(0).getAmount()).isEqualTo(ORDER_AMOUNT);
        }
}
