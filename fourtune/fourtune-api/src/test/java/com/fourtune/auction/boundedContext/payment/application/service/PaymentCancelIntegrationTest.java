package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType;
import com.fourtune.auction.boundedContext.payment.domain.constant.PaymentStatus;
import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Refund;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentGatewayPort;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentRepository;
import com.fourtune.auction.boundedContext.payment.port.out.RefundRepository;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.payment.constant.CashPolicy;
import com.fourtune.shared.payment.dto.OrderDto;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 결제 취소(환불) 통합 테스트
 * - P-S4: 결제 취소 성공 (PG 성공 → 지갑 복원)
 * - P-F10: 취소 금액 초과 시 PAYMENT_CANCEL_AMOUNT_EXCEEDS_BALANCE
 * - P-S5: PG 취소 실패 시 REFUND_PG_RETRY 기록 후 PAYMENT_PG_REFUND_FAILED
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.data.elasticsearch.repositories.enabled=false",
        "feature.kafka.enabled=false"
})
class PaymentCancelIntegrationTest {

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

    @MockitoSpyBean
    private PaymentRefundRetryRecorder paymentRefundRetryRecorder;

    @Autowired
    private PaymentCancelUseCase paymentCancelUseCase;
    @Autowired
    private PaymentUserRepository paymentUserRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private RefundRepository refundRepository;

    private PaymentUser systemUser;
    private PaymentUser customerUser;
    private static final String ORDER_ID = "cancel-test-order-001";
    private static final Long AUCTION_ORDER_ID = 300L;
    private static final Long PAYMENT_AMOUNT = 20_000L;
    private static final String PAYMENT_KEY = "cancel-test-payment-key";

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
        customerUser = paymentUserRepository.findByEmail("cancel-test@test.com")
                .orElseGet(() -> paymentUserRepository.save(
                        PaymentUser.builder()
                                .id(10L)
                                .email("cancel-test@test.com")
                                .nickname("cancel-customer")
                                .password("")
                                .phoneNumber("")
                                .createdAt(now)
                                .updatedAt(now)
                                .deletedAt(null)
                                .status("ACTIVE")
                                .build()));

        // 시스템 지갑: 취소 시 출금할 잔액 확보 (없으면 생성, 있으면 잔액 부족 시 credit으로 채움)
        Wallet sysWallet = walletRepository.findWalletByPaymentUser(systemUser)
                .orElseGet(() -> walletRepository.save(Wallet.builder().paymentUser(systemUser).balance(0L).build()));
        if (sysWallet.getBalance() < 50_000L) {
            sysWallet.credit(50_000L - sysWallet.getBalance(), CashEventType.임시보관__주문결제, "test", 1L);
            walletRepository.saveAndFlush(sysWallet);
        }
        walletRepository.findWalletByPaymentUser(customerUser)
                .orElseGet(() -> walletRepository.save(Wallet.builder().paymentUser(customerUser).balance(0L).build()));

        refundRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    private OrderDto orderDto() {
        return OrderDto.builder()
                .orderId(ORDER_ID)
                .auctionOrderId(AUCTION_ORDER_ID)
                .price(PAYMENT_AMOUNT)
                .userId(customerUser.getId())
                .build();
    }

    private Payment createApprovedPayment() {
        Payment payment = paymentRepository.saveAndFlush(
                Payment.builder()
                        .paymentKey(PAYMENT_KEY)
                        .orderId(ORDER_ID)
                        .auctionOrderId(AUCTION_ORDER_ID)
                        .paymentUser(customerUser)
                        .amount(PAYMENT_AMOUNT)
                        .pgPaymentAmount(PAYMENT_AMOUNT)
                        .status(PaymentStatus.APPROVED)
                        .build());
        return payment;
    }

    @Test
    @DisplayName("[P-S4] 결제 취소 성공: PG cancel 성공 → 지갑 복원, Refund 저장")
    void cancelPayment_success_refundCreatedAndWalletRestored() {
        createApprovedPayment();
        when(paymentGatewayPort.cancel(eq(PAYMENT_KEY), anyString(), eq(PAYMENT_AMOUNT)))
                .thenReturn(PaymentExecutionResult.success(PAYMENT_KEY, ORDER_ID, PAYMENT_AMOUNT));

        Refund refund = paymentCancelUseCase.cancelPayment("고객 요청", null, orderDto());

        assertThat(refund).isNotNull();
        assertThat(refund.getCancelAmount()).isEqualTo(PAYMENT_AMOUNT);
        List<Refund> list = refundRepository.findRefundsByPayment_PaymentUser_Id(customerUser.getId());
        assertThat(list).hasSize(1);

        Payment after = paymentRepository.findPaymentByOrderId(ORDER_ID).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(after.getBalanceAmount()).isZero();

        verify(paymentGatewayPort, times(1)).cancel(eq(PAYMENT_KEY), eq("고객 요청"), eq(PAYMENT_AMOUNT));
    }

    @Test
    @DisplayName("[P-F10] 취소 요청 금액이 취소 가능 잔액 초과 시 PAYMENT_CANCEL_AMOUNT_EXCEEDS_BALANCE")
    void cancelPayment_amountExceedsBalance_throws() {
        createApprovedPayment();
        Long overAmount = PAYMENT_AMOUNT + 5_000L;

        assertThatThrownBy(() -> paymentCancelUseCase.cancelPayment("부분취소", overAmount, orderDto()))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_CANCEL_AMOUNT_EXCEEDS_BALANCE);
                });

        verify(paymentGatewayPort, never()).cancel(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("[P-S5] PG 취소 실패 시 recordRefundPgRetry 호출 후 PAYMENT_PG_REFUND_FAILED")
    void cancelPayment_pgCancelFails_recordsRetryAndThrows() {
        createApprovedPayment();
        when(paymentGatewayPort.cancel(eq(PAYMENT_KEY), anyString(), eq(PAYMENT_AMOUNT)))
                .thenThrow(new RuntimeException("PG timeout"));

        assertThatThrownBy(() -> paymentCancelUseCase.cancelPayment("고객 요청", null, orderDto()))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_PG_REFUND_FAILED);
                });

        verify(paymentGatewayPort, times(1)).cancel(eq(PAYMENT_KEY), anyString(), eq(PAYMENT_AMOUNT));
        verify(paymentRefundRetryRecorder, times(1)).recordRefundPgRetry(eq(PAYMENT_KEY), eq(ORDER_ID), eq(PAYMENT_AMOUNT), anyString());
    }
}
