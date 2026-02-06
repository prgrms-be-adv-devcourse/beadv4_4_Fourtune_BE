package com.fourtune.auction.boundedContext.payment.adapter.in.internal;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentCancelUseCase;
import com.fourtune.auction.boundedContext.payment.domain.entity.Refund;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import com.fourtune.auction.shared.payment.dto.RefundRequest;
import com.fourtune.auction.shared.payment.dto.RefundResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("api/internal/payments")
public class InternalPaymentController {

    private final PaymentCancelUseCase paymentCancelUseCase;

    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> refundPayment(@RequestBody @Valid RefundRequest request) {

        // 1. DTO 변환 (Service 계층의 OrderDto로 변환)
        // OrderDto에 생성자나 Builder가 있다고 가정합니다.
        OrderDto orderDto = OrderDto.builder()
                .orderId(request.getOrderId())
                .auctionOrderId(request.getAuctionOrderId())
                .build();

        // 2. 환불 유스케이스 실행
        // cancelAmount가 null이면 서비스 로직에서 전액 환불로 처리됨
        Refund refund = paymentCancelUseCase.cancelPayment(
                request.getCancelReason(),
                request.getCancelAmount(),
                orderDto
        );

        // 3. 응답 생성
        RefundResponse response = RefundResponse.from(refund);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
