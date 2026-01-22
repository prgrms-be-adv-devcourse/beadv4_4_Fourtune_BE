package com.fourtune.auction.boundedContext.payment.adapter.in.web;

import com.fourtune.auction.boundedContext.payment.adapter.in.web.dto.ConfirmPaymentRequest;
import com.fourtune.auction.boundedContext.payment.adapter.in.web.dto.WalletResponse;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentConfirmUseCase;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.auction.boundedContext.payment.domain.entity.CashLog;
import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.Refund;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.payment.event.PaymentFailedEvent;
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
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentConfirmUseCase paymentConfirmUseCase;
    private final PaymentFacade paymentFacade;
    private final EventPublisher eventPublisher;
    /**
      * 토스페이먼츠 성공 리다이렉트가 아래와 같음
      * URL 예시: /api/payments/toss/success?paymentKey=...&orderId=...&amount=...
      * 위 값을 body에 담아 post 요청 받기
      */


    @PostMapping("/toss/confirm")
    public void tossPaymentSuccess(
            @RequestBody ConfirmPaymentRequest confirmPaymentRequest,
            HttpServletResponse response
    ) {
        String orderNo = confirmPaymentRequest.orderId();
        Long amount = confirmPaymentRequest.amount();
        String paymentKey = confirmPaymentRequest.paymentKey();

        paymentFacade.confirmPayment(paymentKey, orderNo, amount);
    }

    /**
     * 결제 내역 조회 payment 테이블 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<Payment>>> getPayments(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(paymentFacade.findPaymentListByUserId(userId))
        );
    }

    /**
     * 환불 내역 조회 refund 테이블 조회
     */
    @GetMapping("/{userId}/refunds")
    public ResponseEntity<ApiResponse<List<Refund>>> getRefunds(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(paymentFacade.findRefundListByUserId(userId))
        );
    }

    /**
     * 지갑 잔액 조회 API
     */
    @GetMapping("/wallets/{userId}/balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyBalance(@PathVariable("userId") Long userId) {
        Long balance = paymentFacade.getBalance(userId);
        return ResponseEntity.ok(
                ApiResponse.success(WalletResponse.of(balance))
        );
    }

    /**
     * 지갑 상세 내역 조히 API
     */
    @GetMapping("/wallets/{userId}/history")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletHistory(@PathVariable("userId") Long userId) {
        List<CashLog> cashLogs = paymentFacade.getRecentCashLogs(userId, 10);
        return ResponseEntity.ok(ApiResponse.success(WalletResponse.of(cashLogs))
        );
    }

    /**
     * 지갑 잔액 + 상세 내역 조히 API
     */
    @GetMapping("/wallets/{userId}/summary")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletSummary(@PathVariable("userId") Long userId) {
        Wallet wallet = paymentFacade.findWalletByUserId(userId).orElseThrow();
        List<CashLog> cashLogs = paymentFacade.getRecentCashLogs(userId, 10);
        return ResponseEntity.ok(ApiResponse.success(WalletResponse.of(wallet.getBalance(), cashLogs))
        );
    }
}
