package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.domain.entity.Refund;
import com.fourtune.payment.port.out.AuctionPort;
import com.fourtune.shared.payment.dto.OrderDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentFacade cancelPayment 단위 테스트")
class PaymentFacadeCancelTest {

    @Mock
    private AuctionPort auctionPort;
    @Mock
    private PaymentCancelUseCase paymentCancelUseCase;

    @InjectMocks
    private PaymentFacade sut;

    private static OrderDto orderDto() {
        return OrderDto.builder()
                .orderId("order-1")
                .auctionOrderId(100L)
                .userId(1L)
                .price(10_000L)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("auctionPort.getOrder가 null이면 PAYMENT_AUCTION_ORDER_NOT_FOUND")
    void orderNotFound() {
        when(auctionPort.getOrder("order-1")).thenReturn(null);

        assertThatThrownBy(() -> sut.cancelPayment("order-1", "reason", null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND));

        verify(paymentCancelUseCase, never()).cancelPayment(any(), any(), any());
    }

    @Test
    @DisplayName("OrderDto가 있으면 cancelUseCase 호출 후 Refund 반환")
    void success_delegatesToUseCase() {
        OrderDto dto = orderDto();
        Refund refund = Refund.builder().cancelAmount(10_000L).cancelReason("reason").build();
        when(auctionPort.getOrder("order-1")).thenReturn(dto);
        when(paymentCancelUseCase.cancelPayment(eq("reason"), eq(10_000L), eq(dto))).thenReturn(refund);

        Refund result = sut.cancelPayment("order-1", "reason", 10_000L);

        assertThat(result).isSameAs(refund);
        verify(paymentCancelUseCase).cancelPayment("reason", 10_000L, dto);
    }
}
