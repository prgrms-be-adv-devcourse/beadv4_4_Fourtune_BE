package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.domain.constant.PaymentStatus;
import com.fourtune.payment.domain.entity.Payment;
import com.fourtune.payment.domain.entity.PaymentUser;
import com.fourtune.payment.domain.entity.Refund;
import com.fourtune.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.payment.port.out.PaymentGatewayPort;
import com.fourtune.payment.port.out.PaymentRepository;
import com.fourtune.shared.payment.dto.OrderDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCancelUseCase 단위 테스트")
class PaymentCancelUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGatewayPort paymentGatewayPort;
    @Mock
    private PaymentRefundRetryRecorder paymentRefundRetryRecorder;
    @Mock
    private PaymentCancelCompletion paymentCancelCompletion;

    @InjectMocks
    private PaymentCancelUseCase sut;

    private static final String ORDER_ID = "order-uuid-1";
    private static final Long AUCTION_ORDER_ID = 100L;
    private static final Long USER_ID = 1L;

    private OrderDto orderDto() {
        return OrderDto.builder()
                .orderId(ORDER_ID)
                .auctionOrderId(AUCTION_ORDER_ID)
                .userId(USER_ID)
                .price(10_000L)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    private PaymentUser paymentUser() {
        return PaymentUser.builder()
                .id(USER_ID)
                .email("test@test.com")
                .nickname("user1")
                .password("")
                .phoneNumber("")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .status("ACTIVE")
                .build();
    }

    private Payment approvedPayment(Long amount) {
        return Payment.builder()
                .paymentKey("pk-" + ORDER_ID)
                .orderId(ORDER_ID)
                .auctionOrderId(AUCTION_ORDER_ID)
                .paymentUser(paymentUser())
                .amount(amount)
                .pgPaymentAmount(amount)
                .status(PaymentStatus.APPROVED)
                .build();
    }

    @Nested
    @DisplayName("cancelPayment")
    class CancelPayment {

        @Test
        @DisplayName("결제가 없으면 PAYMENT_NOT_FOUND")
        void paymentNotFound() {
            when(paymentRepository.findPaymentByOrderId(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.cancelPayment("단순 변심", null, orderDto()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND));

            verify(paymentGatewayPort, never()).cancel(any(), any(), any());
            verify(paymentCancelCompletion, never()).completeCancelInDb(any(), any(), any(), any());
        }

        @Test
        @DisplayName("이미 전액 취소된 결제면 PAYMENT_ALREADY_CANCELED")
        void alreadyCanceled() {
            Payment canceled = Payment.builder()
                    .paymentKey("pk-1")
                    .orderId(ORDER_ID)
                    .auctionOrderId(AUCTION_ORDER_ID)
                    .paymentUser(paymentUser())
                    .amount(10_000L)
                    .pgPaymentAmount(10_000L)
                    .status(PaymentStatus.CANCELED)
                    .build();
            when(paymentRepository.findPaymentByOrderId(ORDER_ID)).thenReturn(Optional.of(canceled));

            assertThatThrownBy(() -> sut.cancelPayment("reason", null, orderDto()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_ALREADY_CANCELED));

            verify(paymentGatewayPort, never()).cancel(any(), any(), any());
            verify(paymentCancelCompletion, never()).completeCancelInDb(any(), any(), any(), any());
        }

        @Test
        @DisplayName("취소 요청 금액이 잔액 초과면 PAYMENT_CANCEL_AMOUNT_EXCEEDS_BALANCE")
        void cancelAmountExceedsBalance() {
            Payment payment = approvedPayment(10_000L); // balanceAmount = 10_000
            when(paymentRepository.findPaymentByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> sut.cancelPayment("reason", 20_000L, orderDto()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_CANCEL_AMOUNT_EXCEEDS_BALANCE));

            verify(paymentGatewayPort, never()).cancel(any(), any(), any());
            verify(paymentCancelCompletion, never()).completeCancelInDb(any(), any(), any(), any());
        }

        @Test
        @DisplayName("PG 취소 호출이 예외를 던지면 recordRefundPgRetry 후 PAYMENT_PG_REFUND_FAILED")
        void pgCancelThrows() {
            Payment payment = approvedPayment(10_000L);
            when(paymentRepository.findPaymentByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
            when(paymentGatewayPort.cancel(anyString(), anyString(), anyLong())).thenThrow(new RuntimeException("PG timeout"));

            assertThatThrownBy(() -> sut.cancelPayment("reason", 10_000L, orderDto()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_PG_REFUND_FAILED));

            verify(paymentRefundRetryRecorder).recordRefundPgRetry(eq(payment.getPaymentKey()), eq(ORDER_ID), eq(10_000L), eq("reason"));
            verify(paymentCancelCompletion, never()).completeCancelInDb(any(), any(), any(), any());
        }

        @Test
        @DisplayName("PG 취소 결과가 실패면 PAYMENT_PG_REFUND_FAILED")
        void pgCancelReturnsFailure() {
            Payment payment = approvedPayment(10_000L);
            when(paymentRepository.findPaymentByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
            when(paymentGatewayPort.cancel(anyString(), anyString(), anyLong()))
                    .thenReturn(new PaymentExecutionResult("pk", ORDER_ID, 10_000L, false, "PG 거절"));

            assertThatThrownBy(() -> sut.cancelPayment("reason", 10_000L, orderDto()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_PG_REFUND_FAILED));

            verify(paymentCancelCompletion, never()).completeCancelInDb(any(), any(), any(), any());
        }

        @Test
        @DisplayName("PG 취소 성공 시 completeCancelInDb 호출 후 Refund 반환")
        void success_returnsRefund() {
            OrderDto dto = orderDto();
            Payment payment = approvedPayment(10_000L);
            Refund refund = Refund.create(payment, 10_000L, "reason", null);
            when(paymentRepository.findPaymentByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
            when(paymentGatewayPort.cancel(eq(payment.getPaymentKey()), eq("reason"), eq(10_000L)))
                    .thenReturn(PaymentExecutionResult.success(payment.getPaymentKey(), ORDER_ID, 10_000L));
            when(paymentCancelCompletion.completeCancelInDb(eq(payment), eq(dto), eq(10_000L), eq("reason")))
                    .thenReturn(refund);

            Refund result = sut.cancelPayment("reason", 10_000L, dto);

            assertThat(result).isSameAs(refund);
            verify(paymentCancelCompletion).completeCancelInDb(payment, dto, 10_000L, "reason");
        }

        @Test
        @DisplayName("cancelAmount null이면 전액 취소(잔액 전체)로 요청")
        void success_fullCancelWhenAmountNull() {
            OrderDto dto = orderDto();
            Payment payment = approvedPayment(15_000L);
            Refund refund = Refund.create(payment, 15_000L, "전액", null);
            when(paymentRepository.findPaymentByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
            when(paymentGatewayPort.cancel(anyString(), anyString(), eq(15_000L)))
                    .thenReturn(PaymentExecutionResult.success(payment.getPaymentKey(), ORDER_ID, 15_000L));
            when(paymentCancelCompletion.completeCancelInDb(eq(payment), eq(dto), eq(15_000L), eq("전액")))
                    .thenReturn(refund);

            Refund result = sut.cancelPayment("전액", null, dto);

            assertThat(result).isSameAs(refund);
            verify(paymentGatewayPort).cancel(payment.getPaymentKey(), "전액", 15_000L);
            verify(paymentCancelCompletion).completeCancelInDb(payment, dto, 15_000L, "전액");
        }
    }
}
