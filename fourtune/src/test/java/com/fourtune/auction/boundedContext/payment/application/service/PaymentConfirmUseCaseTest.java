package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.payment.domain.constant.PaymentStatus;
import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.*;
import com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.payment.constant.CashPolicy;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import com.fourtune.auction.shared.payment.event.PaymentFailedEvent;
import com.fourtune.auction.shared.payment.event.PaymentSucceededEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class PaymentConfirmUseCaseTest {

    @Autowired PaymentConfirmUseCase paymentConfirmUseCase;

    // DB 검증용 레포지토리
    @Autowired PaymentRepository paymentRepository;
    @Autowired WalletRepository walletRepository;
    @Autowired PaymentUserRepository paymentUserRepository;
    @Autowired PaymentSupport paymentSupport;

    // 외부 시스템 Mocking
    @MockitoBean PaymentGatewayPort paymentGatewayPort;
    @MockitoBean AuctionPort auctionPort;
    @MockitoBean EventPublisher eventPublisher;

    // 테스트 상수
    final String ORDER_NO = "ORD-2025-001";
    final Long ORDER_ID = 100L;
    final Long USER_ID = 20L;
    final Long AMOUNT = 50000L;
    final Long SELLER_ID = 21L;
    final String PAYMENT_KEY = "test_payment_key";
    final Long SYSTEM_ID = 1L;

    @BeforeEach
    void setUp() {
        // 1. 구매자(User) 및 지갑 생성
        createTestUserAndWallet(USER_ID, "buyer@test.com", 0L); // 잔액 0원 (충전 후 즉시 결제 시나리오)
        createTestUserAndWallet(SELLER_ID, "seller@test.com", 0L);
//         2. 시스템(System) 유저 및 지갑 생성 (필수조건)
        createTestUserAndWallet(SYSTEM_ID, CashPolicy.SYSTEM_HOLDING_USER_EMAIL, 0L);
    }

    @Test
    @DisplayName("[성공] 결제 승인 -> 내부 검증 통과 -> 지갑 처리 -> 이벤트 발행 성공")
    void confirmPayment_Success() {
        // Given: 상황 설정

        // 1. 경매 모듈(AuctionPort)이 정상적인 주문 정보를 리턴
        // [수정] OrderItem 리스트 생성하여 OrderDto에 포함
        OrderDto.OrderItem mockItem = OrderDto.OrderItem.builder()
                .itemId(10L)
                .sellerId(SELLER_ID)
                .price(AMOUNT)
                .itemName("테스트 경매 물품")
                .build();

        OrderDto mockOrder = OrderDto.builder()
                .orderId(ORDER_ID)
                .orderNo(ORDER_NO)
                .userId(USER_ID)
                .price(AMOUNT)
                .orderStatus(OrderStatus.PENDING)
                .items(List.of(mockItem)) // Items 추가
                .build();

        given(auctionPort.getOrder(ORDER_NO)).willReturn(mockOrder);

        // 2. PG사(Toss) 승인 성공 모킹
        given(paymentGatewayPort.confirm(anyString(), anyString(), any()))
                .willReturn(new PaymentExecutionResult(PAYMENT_KEY, ORDER_NO, AMOUNT, true));

        // When: 결제 승인 요청 실행
        paymentConfirmUseCase.confirmPayment(PAYMENT_KEY, ORDER_NO, AMOUNT, USER_ID);

        // Then: 검증

        // 1. Payment 데이터가 저장되었는가?
        Payment payment = paymentRepository.findByPaymentKey(PAYMENT_KEY).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getAmount()).isEqualTo(AMOUNT);

        // 2. 시스템 지갑에 돈이 들어왔는가? (임시 보관)
        Wallet systemWallet = paymentSupport.findWalletByUserEmail(CashPolicy.SYSTEM_HOLDING_USER_EMAIL).orElseThrow();
        assertThat(systemWallet.getBalance()).isEqualTo(AMOUNT);

        // 3. 성공 이벤트가 발행되었는가?
        verify(eventPublisher, times(1)).publish(any(PaymentSucceededEvent.class));
    }

    @Test
    @DisplayName("[실패/보상] 주문 금액 불일치 -> 예외 발생 -> PG 취소 호출 -> 실패 이벤트")
    void confirmPayment_AmountMismatch_Rollback() {
        // Given
        Long wrongAmount = 10000L; // 실제 PG 결제 금액 (요청 금액)
        Long orderPrice = 50000L;  // 실제 주문 가격

        // 1. PG사는 승인 성공했다고 가정 (돈 나감)
        given(paymentGatewayPort.confirm(anyString(), anyString(), any()))
                .willReturn(new PaymentExecutionResult(PAYMENT_KEY, ORDER_NO, wrongAmount, true));

        // 2. 경매 모듈은 5만원짜리 주문 정보를 줌
        OrderDto mockOrder = OrderDto.builder()
                .orderId(ORDER_ID).orderNo(ORDER_NO).userId(USER_ID)
                .price(orderPrice) // 50,000원
                .orderStatus(OrderStatus.PENDING)
                .build();
        given(auctionPort.getOrder(ORDER_NO)).willReturn(mockOrder);

        // When & Then: 실행 시 예외 발생 검증
        assertThatThrownBy(() ->
                paymentConfirmUseCase.confirmPayment(PAYMENT_KEY, ORDER_NO, wrongAmount, USER_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_AMOUNT_MISMATCH);

        // Verify: 보상 트랜잭션 검증

        // 1. PG사 취소(cancel) 메서드가 호출되었는가? (가장 중요)
        verify(paymentGatewayPort, times(1)).cancel(eq(PAYMENT_KEY), contains("System Logic Failed"));

        // 2. 실패 이벤트(PaymentFailedEvent)가 발행되었는가?
        verify(eventPublisher, times(1)).publish(any(PaymentFailedEvent.class));

        // 3. DB 롤백 확인 (Payment 데이터가 없어야 함)
        assertThat(paymentRepository.findByPaymentKey(PAYMENT_KEY)).isEmpty();
    }

    @Test
    @DisplayName("[실패/치명적] 내부 오류 후 PG 취소마저 실패 -> P311 에러 발생")
    void confirmPayment_CancelFailed_CriticalError() {
        // Given

        // 1. PG 승인 성공
        given(paymentGatewayPort.confirm(anyString(), anyString(), any()))
                .willReturn(new PaymentExecutionResult(PAYMENT_KEY, ORDER_NO, AMOUNT, true));

        // 2. 내부 로직 실패 유도 (경매 정보 없음 -> PAYMENT_AUCTION_ORDER_NOT_FOUND)
        given(auctionPort.getOrder(ORDER_NO)).willReturn(null);

        // 3. [핵심] PG 취소 요청 시 예외 발생하도록 설정 (네트워크 오류 가정)
        doThrow(new RuntimeException("PG Connection Timeout"))
                .when(paymentGatewayPort).cancel(anyString(), anyString());

        // When & Then
        assertThatThrownBy(() ->
                paymentConfirmUseCase.confirmPayment(PAYMENT_KEY, ORDER_NO, AMOUNT, USER_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_PG_REFUND_FAILED); // 코드에서 던지는 에러 확인

        // Verify

        // 1. 관리자 문의용(P311) 실패 이벤트가 발행되었는가?
        verify(eventPublisher, times(1)).publish(argThat(event -> {
            return event instanceof PaymentFailedEvent
                    && ((PaymentFailedEvent) event).getResultCode().equals("P311");
        }));
    }

    // --- Helper Methods ---

    private void createTestUserAndWallet(Long userId, String email, Long balance) {
        // User 생성
        PaymentUser user = PaymentUser.builder()
                .id(userId)
                .email(email)
                .password("")
                .phoneNumber("")
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .deletedAt(null)
                .status("ACTIVE")
                .nickname("TestUser" + userId)
                .build();
        paymentUserRepository.save(user);

        // Wallet 생성
        Wallet wallet = Wallet.builder()
                .paymentUser(user)
                .balance(balance)
                .build();
        walletRepository.save(wallet);
    }
}