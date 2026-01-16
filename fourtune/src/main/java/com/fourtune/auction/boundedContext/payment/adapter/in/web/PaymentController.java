package com.fourtune.auction.boundedContext.payment.adapter.in.web;

import com.fourtune.auction.boundedContext.payment.adapter.in.web.dto.ConfirmPaymentRequest;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentConfirmUseCase;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentConfirmUseCase;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentConfirmUseCase paymentConfirmUseCase;
    /**
      * 토스페이먼츠 성공 리다이렉트 처리 (백엔드 처리 후 프론트로 이동)
      * URL 예시: /api/payments/toss/success?paymentKey=...&orderId=...&amount=...
      */
    @PostMapping("/payment/confirm/by/tosspayments")
    public void tossPaymentSuccess(
            @RequestBody ConfirmPaymentRequest confirmPaymentRequest,
            HttpServletResponse response
    ) throws IOException {
        String orderId = confirmPaymentRequest.orderId();
        Long amount = confirmPaymentRequest.amount();
        String paymentKey = confirmPaymentRequest.paymentKey();

        log.info("토스 결제 성공 리다이렉트 받음 - orderId: {}, amount: {}", orderId, amount);

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
            //TODO: 결제 실패 event
        }
    }
}