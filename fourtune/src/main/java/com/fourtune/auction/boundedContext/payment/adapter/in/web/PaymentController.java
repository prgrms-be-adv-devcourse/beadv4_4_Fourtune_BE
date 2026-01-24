package com.fourtune.auction.boundedContext.payment.adapter.in.web;

import com.fourtune.auction.boundedContext.payment.adapter.in.web.dto.ConfirmPaymentRequest;
import com.fourtune.auction.boundedContext.payment.adapter.in.web.dto.WalletResponse;
import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.auction.boundedContext.payment.domain.entity.CashLog;
import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.Refund;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.shared.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentFacade paymentFacade;
    /**
      * 토스페이먼츠 성공 리다이렉트가 아래와 같음
      * URL 예시: /api/payments/toss/success?paymentKey=...&orderId=...&amount=...
      * 위 값을 body에 담아 post 요청 받기
      */


    @PostMapping("/toss/confirm")
    public void tossPaymentSuccess(
            @AuthenticationPrincipal UserContext user,
            @RequestBody ConfirmPaymentRequest confirmPaymentRequest
    ) {
        String orderNo = confirmPaymentRequest.orderId();
        Long amount = confirmPaymentRequest.amount();
        String paymentKey = confirmPaymentRequest.paymentKey();

        paymentFacade.confirmPayment(paymentKey, orderNo, amount, user.id());
    }

    /**
     * 결제 내역 조회 payment 테이블 조회
     */
    @GetMapping()
    public ResponseEntity<ApiResponse<List<Payment>>> getPayments(@AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(
                ApiResponse.success(paymentFacade.findPaymentListByUserId(user.id()))
        );
    }

    /**
     * 환불 내역 조회 refund 테이블 조회
     */
    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<List<Refund>>> getRefunds(@AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(
                ApiResponse.success(paymentFacade.findRefundListByUserId(user.id()))
        );
    }

    /**
     * 지갑 잔액 조회 API
     */
    @GetMapping("/wallets/balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyBalance(@AuthenticationPrincipal UserContext user) {
        Long balance = paymentFacade.getBalance(user.id());
        return ResponseEntity.ok(
                ApiResponse.success(WalletResponse.of(balance))
        );
    }

    /**
     * 지갑 상세 내역 조히 API
     */
    @GetMapping("/wallets/history")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletHistory(@AuthenticationPrincipal UserContext user) {
        List<CashLog> cashLogs = paymentFacade.getRecentCashLogs(user.id(), 10);
        return ResponseEntity.ok(ApiResponse.success(WalletResponse.of(cashLogs))
        );
    }

    /**
     * 지갑 잔액 + 상세 내역 조히 API
     */
    @GetMapping("/wallets/summary")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletSummary(@AuthenticationPrincipal UserContext user) {
        Wallet wallet = paymentFacade.findWalletByUserId(user.id()).orElseThrow();
        List<CashLog> cashLogs = paymentFacade.getRecentCashLogs(user.id(), 10);
        return ResponseEntity.ok(ApiResponse.success(WalletResponse.of(wallet.getBalance(), cashLogs))
        );
    }
}
