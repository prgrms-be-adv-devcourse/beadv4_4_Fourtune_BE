package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentGatewayPort;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.payment.dto.OrderDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 결제 승인 실패 시나리오 (문서 P-F1, P-F3, P-F4)
 * - P-F1: 주문 없음 / 주문자·결제자 불일치
 * - P-F3: 주문 상태 비 PENDING
 * - P-F4: 결제 금액 불일치
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.data.elasticsearch.repositories.enabled=false",
        "feature.kafka.enabled=false"
})
class PaymentConfirmFailureScenariosTest {

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

    private static final String ORDER_ID = "fail-scenario-order-001";
    private static final String PAYMENT_KEY = "fail-payment-key-001";
    private static final Long PG_AMOUNT = 10_000L;
    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("[P-F1] 주문 없음: getOrder null 시 PAYMENT_AUCTION_ORDER_NOT_FOUND")
    void confirmPayment_orderNotFound_throwsAuctionOrderNotFound() {
        when(paymentGatewayPort.confirm(eq(PAYMENT_KEY), eq(ORDER_ID), eq(PG_AMOUNT)))
                .thenReturn(com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult.success(PAYMENT_KEY, ORDER_ID, PG_AMOUNT));
        when(auctionPort.getOrder(eq(ORDER_ID))).thenReturn(null);

        assertThatThrownBy(() -> paymentConfirmUseCase.confirmPayment(PAYMENT_KEY, ORDER_ID, PG_AMOUNT, USER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND);
                });

        verify(paymentGatewayPort).confirm(anyString(), anyString(), anyLong());
        verify(auctionPort).getOrder(eq(ORDER_ID));
    }

    @Test
    @DisplayName("[P-F1] 주문자·결제자 불일치: userId 다르면 PAYMENT_PURCHASE_NOT_ALLOWED")
    void confirmPayment_userMismatch_throwsPurchaseNotAllowed() {
        when(paymentGatewayPort.confirm(eq(PAYMENT_KEY), eq(ORDER_ID), eq(PG_AMOUNT)))
                .thenReturn(com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult.success(PAYMENT_KEY, ORDER_ID, PG_AMOUNT));
        OrderDto orderDto = OrderDto.builder()
                .orderId(ORDER_ID)
                .auctionOrderId(100L)
                .price(PG_AMOUNT)
                .userId(999L)
                .orderStatus("PENDING")
                .build();
        when(auctionPort.getOrder(eq(ORDER_ID))).thenReturn(orderDto);

        assertThatThrownBy(() -> paymentConfirmUseCase.confirmPayment(PAYMENT_KEY, ORDER_ID, PG_AMOUNT, USER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_PURCHASE_NOT_ALLOWED);
                });
    }

    @Test
    @DisplayName("[P-F3] 주문 상태 비 PENDING: PAYMENT_ORDER_NOT_PENDING")
    void confirmPayment_orderNotPending_throwsOrderNotPending() {
        when(paymentGatewayPort.confirm(eq(PAYMENT_KEY), eq(ORDER_ID), eq(PG_AMOUNT)))
                .thenReturn(com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult.success(PAYMENT_KEY, ORDER_ID, PG_AMOUNT));
        OrderDto orderDto = OrderDto.builder()
                .orderId(ORDER_ID)
                .auctionOrderId(100L)
                .price(PG_AMOUNT)
                .userId(USER_ID)
                .orderStatus("COMPLETED")
                .build();
        when(auctionPort.getOrder(eq(ORDER_ID))).thenReturn(orderDto);

        assertThatThrownBy(() -> paymentConfirmUseCase.confirmPayment(PAYMENT_KEY, ORDER_ID, PG_AMOUNT, USER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_ORDER_NOT_PENDING);
                });
    }

    @Test
    @DisplayName("[P-F4] 결제 금액 불일치: pgAmount != order.price 시 PAYMENT_AMOUNT_MISMATCH")
    void confirmPayment_amountMismatch_throwsAmountMismatch() {
        when(paymentGatewayPort.confirm(eq(PAYMENT_KEY), eq(ORDER_ID), eq(PG_AMOUNT)))
                .thenReturn(com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult.success(PAYMENT_KEY, ORDER_ID, PG_AMOUNT));
        OrderDto orderDto = OrderDto.builder()
                .orderId(ORDER_ID)
                .auctionOrderId(100L)
                .price(15_000L)
                .userId(USER_ID)
                .orderStatus("PENDING")
                .build();
        when(auctionPort.getOrder(eq(ORDER_ID))).thenReturn(orderDto);

        assertThatThrownBy(() -> paymentConfirmUseCase.confirmPayment(PAYMENT_KEY, ORDER_ID, PG_AMOUNT, USER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
                });

        verify(auctionPort).getOrder(eq(ORDER_ID));
        // 내부 로직 실패(금액 불일치 포함) 시 보상으로 cancel() 1회 호출됨
        verify(paymentGatewayPort, times(1)).cancel(eq(PAYMENT_KEY), startsWith("System Logic Failed:"), eq(null));
    }
}
