package com.fourtune.payment.adapter.in.web;

import com.fourtune.core.dto.ApiResponse;
import com.fourtune.payment.adapter.in.web.dto.ConfirmPaymentRequest;
import com.fourtune.payment.adapter.in.web.dto.PaymentConfirmResponse;
import com.fourtune.payment.adapter.in.web.dto.WalletChargeRequest;
import com.fourtune.payment.adapter.in.web.dto.WalletPayRequest;
import com.fourtune.payment.adapter.in.web.dto.WalletResponse;
import com.fourtune.payment.application.service.PaymentFacade;
import com.fourtune.payment.domain.entity.CashLog;
import com.fourtune.payment.domain.entity.Payment;
import com.fourtune.payment.domain.entity.Wallet;
import com.fourtune.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.shared.auth.dto.UserContext;
import com.fourtune.shared.payment.dto.PaymentDto;
import jakarta.validation.Valid;
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
     * Toss PG + 예치금 혼합 결제 API.
     * 부족분만 Toss로 결제한 뒤 지갑 잔액과 합산해 주문금액을 결제한다.
     */
    @PostMapping("/toss/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> tossPaymentSuccess(
            @AuthenticationPrincipal UserContext user,
            @RequestBody ConfirmPaymentRequest confirmPaymentRequest
    ) {
        String orderId = confirmPaymentRequest.orderId();
        Long amount = confirmPaymentRequest.amount();
        String paymentKey = confirmPaymentRequest.paymentKey();

        PaymentExecutionResult result = paymentFacade.confirmPayment(paymentKey, orderId, amount, user.id());
        return ResponseEntity.ok(ApiResponse.success(PaymentConfirmResponse.from(result)));
    }

    /**
     * 예치금(지갑 잔액)만으로 결제하는 API.
     * Toss 결제창 없이 지갑 잔액이 충분할 때 사용한다.
     */
    @PostMapping("/wallet/pay")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> walletPay(
            @AuthenticationPrincipal UserContext user,
            @Valid @RequestBody WalletPayRequest request
    ) {
        Payment payment = paymentFacade.walletPay(user.id(), request.orderId());
        return ResponseEntity.ok(ApiResponse.success(
                PaymentConfirmResponse.builder()
                        .paymentKey(payment.getPaymentKey())
                        .orderId(payment.getOrderId())
                        .amount(payment.getAmount())
                        .success(true)
                        .build()
        ));
    }

    /**
     * 지갑 사전 충전 API.
     * 낙찰과 무관하게 원하는 금액을 미리 예치금으로 충전한다.
     */
    @PostMapping("/wallet/charge")
    public ResponseEntity<ApiResponse<WalletResponse>> chargeWallet(
            @AuthenticationPrincipal UserContext user,
            @Valid @RequestBody WalletChargeRequest request
    ) {
        Wallet wallet = paymentFacade.chargeWallet(user.id(), request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.ok(ApiResponse.success(WalletResponse.of(wallet.getBalance())));
    }

    /**
     * 결제 내역 조회
     */
    @GetMapping()
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPayments(@AuthenticationPrincipal UserContext user) {
        List<PaymentDto> payments = paymentFacade.findPaymentListByUserId(user.id())
                .stream()
                .map(Payment::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * 환불 내역 조회
     */
    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<List<com.fourtune.payment.adapter.in.web.dto.RefundResponse>>> getRefunds(
            @AuthenticationPrincipal UserContext user
    ) {
        List<com.fourtune.payment.adapter.in.web.dto.RefundResponse> refunds = paymentFacade.findRefundListByUserId(user.id())
                .stream()
                .map(com.fourtune.payment.adapter.in.web.dto.RefundResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(refunds));
    }

    /**
     * 지갑 잔액 조회 API
     */
    @GetMapping("/wallets/balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyBalance(@AuthenticationPrincipal UserContext user) {
        Long balance = paymentFacade.getBalance(user.id());
        return ResponseEntity.ok(ApiResponse.success(WalletResponse.of(balance)));
    }

    /**
     * 지갑 상세 내역 조회 API
     */
    @GetMapping("/wallets/history")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletHistory(@AuthenticationPrincipal UserContext user) {
        List<CashLog> cashLogs = paymentFacade.getRecentCashLogs(user.id(), 10);
        return ResponseEntity.ok(ApiResponse.success(WalletResponse.of(cashLogs)));
    }

    /**
     * 지갑 잔액 + 상세 내역 요약 API
     */
    @GetMapping("/wallets/summary")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletSummary(@AuthenticationPrincipal UserContext user) {
        Wallet wallet = paymentFacade.findWalletByUserId(user.id()).orElseThrow();
        List<CashLog> cashLogs = paymentFacade.getRecentCashLogs(user.id(), 10);
        return ResponseEntity.ok(ApiResponse.success(WalletResponse.of(wallet.getBalance(), cashLogs)));
    }
}
