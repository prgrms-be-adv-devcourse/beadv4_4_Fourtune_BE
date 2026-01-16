package com.fourtune.auction.boundedContext.payment.adapter.in.web;

import com.fourtune.auction.boundedContext.payment.adapter.in.web.dto.ConfirmPaymentRequest;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentConfirmUseCase;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentConfirmUseCase;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.payment.event.PaymentCashFailedEvent;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentConfirmUseCase paymentConfirmUseCase;
    private final EventPublisher eventPublisher;
    /**
      * 토스페이먼츠 성공 리다이렉트가 아래와 같음
      * URL 예시: /api/payments/toss/success?paymentKey=...&orderId=...&amount=...
      * 위 값을 body에 담아 post 요청 받기
      */


    @PostMapping("/confirm/by/tosspayments")
    public void tossPaymentSuccess(
            @RequestBody ConfirmPaymentRequest confirmPaymentRequest,
            HttpServletResponse response
    ) throws IOException {
        Long orderId = confirmPaymentRequest.orderId();
        Long amount = confirmPaymentRequest.amount();
        String paymentKey = confirmPaymentRequest.paymentKey();

        try {// 1. 토스 서버에 결제 승인 요청 (Business Logic)
             // 이 메서드 안에서 RestTemplate 등을 사용해 토스 승인 API를 호출해야 합니다.
             paymentConfirmUseCase.confirmPayment(paymentKey, orderId, amount);

             // 2. 결제 승인 성공 시 -> 프론트엔드 성공 페이지로 리다이렉트
             // 프론트 주소가 http://localhost:3000 이라면 아래처럼 설정
             response.sendRedirect("http://localhost:3000/order/success?orderId=" + orderId);

        } catch (Exception e) {
            log.error("결제 승인 실패", e);
            // 3. 실패 시 -> 프론트엔드 실패 페이지로 리다이렉트
            response.sendRedirect("http://localhost:3000/order/fail?message=" + e.getMessage());

            eventPublisher.publish(
                    new PaymentCashFailedEvent(
                            "400-1",
                            "결제 승인 실패 : %번 주문이 결제 내역과 일치하지 않습니다.".formatted(orderId),
                            null,
                            amount,
                           0L
                    )
            );
        }
    }
}