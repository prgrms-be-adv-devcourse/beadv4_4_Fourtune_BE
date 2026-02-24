package com.fourtune.payment.adapter.in.web;

import com.fourtune.payment.application.service.PaymentFacade;
import com.fourtune.payment.adapter.in.web.dto.CancelPaymentRequest;
import com.fourtune.payment.domain.entity.Refund;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 내부 전용 API. 다른 서비스(auction, fourtune-api 등)에서 결제 취소를 요청할 때 사용.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/payments")
public class InternalPaymentController {

    private final PaymentFacade paymentFacade;

    @Value("${payment.internal.token:}")
    private String internalToken;

    @PostMapping("/cancel")
    public ResponseEntity<Refund> cancelPayment(
            @Valid @RequestBody CancelPaymentRequest request,
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            HttpServletRequest httpRequest
    ) {
        if (isInternalTokenRequired() && !isValidInternalToken(token)) {
            log.warn("내부 API 무단 접근 차단: X-Internal-Token 불일치 또는 없음, path={}", httpRequest.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("결제 취소 요청 수신: orderId={}, cancelReason={}", request.orderId(), request.cancelReason());
        Refund refund = paymentFacade.cancelPayment(
                request.orderId(),
                request.cancelReason(),
                request.cancelAmount()
        );
        return ResponseEntity.ok(refund);
    }

    private boolean isInternalTokenRequired() {
        return internalToken != null && !internalToken.isBlank();
    }

    private boolean isValidInternalToken(String token) {
        return token != null && token.equals(internalToken);
    }
}
