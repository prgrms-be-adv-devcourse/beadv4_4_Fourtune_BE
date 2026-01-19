package com.fourtune.auction.boundedContext.payment.adapter.in.web;

import com.fourtune.auction.boundedContext.payment.adapter.in.web.dto.ConfirmPaymentRequest;
import com.fourtune.auction.boundedContext.payment.adapter.in.web.dto.WalletResponse;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentConfirmUseCase;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.auction.boundedContext.payment.domain.entity.CashLog;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.payment.event.PaymentCashFailedEvent;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentConfirmUseCase paymentConfirmUseCase;
    private final PaymentFacade paymentFacade;
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
            paymentFacade.confirmPayment(paymentKey, orderId, amount);

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

    /**
     * 지갑 잔액 조회 API
     */
    @GetMapping("/wallet/{userId}/balance")
    public ApiResponse<WalletResponse> getMyBalance(@PathVariable("userId") Long userId) {
        Long balance = paymentFacade.getBalance(userId);
        return ApiResponse.success(WalletResponse.of(balance));
    }

    /**
     * 지갑 상세 내역 조히 API TODO: 무한 스크롤/페이징 API
     */
    @GetMapping("/wallet/{userId}/history")
    public ApiResponse<WalletResponse> getWalletHistory(@PathVariable("userId") Long userId) {
        List<CashLog> cashLogs = paymentFacade.getCashLogList(userId);
        return ApiResponse.success(WalletResponse.of(cashLogs));
    }

    /**
     * 지갑 잔액 + 상세 내역 조히 API TODO: 최신 10개 로그만
     */
    @GetMapping("/wallet/{userId}/summary")
    public ApiResponse<WalletResponse> getWalletSummary(@PathVariable("userId") Long userId) {
        Wallet wallet = paymentFacade.findWalletByUserId(userId).orElseThrow();
        return ApiResponse.success(WalletResponse.of(wallet.getBalance(), wallet.getCashLogs()));
    }
}