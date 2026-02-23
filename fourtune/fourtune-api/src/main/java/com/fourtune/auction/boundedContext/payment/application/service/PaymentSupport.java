package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.*;
import com.fourtune.shared.payment.constant.CashPolicy;
import com.fourtune.auction.boundedContext.payment.port.out.*;
import com.fourtune.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentSupport {
    private final PaymentUserRepository paymentUserRepository;
    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final CashLogRepository cashLogRepository;

    public Optional<PaymentUser> findUserByEmail(String email) {
        return paymentUserRepository.findByEmail(email);
    }

    public Optional<PaymentUser> findUserByUserId(Long userId) {
        return paymentUserRepository.findById(userId);
    }

    public Optional<Wallet> findWalletByUser(PaymentUser paymentUser) {
        return walletRepository.findWalletByPaymentUser(paymentUser);
    }

    public Optional<Wallet> findWalletByUserEmail(String email) {
        PaymentUser user = findUserByEmail(email).orElseThrow();
        return findWalletByUser(user);
    }

    /** 이메일 기준 지갑 조회 (비관적 락, 정산 지급 등 동시성 제어용) */
    public Optional<Wallet> findWalletByUserEmailForUpdate(String email) {
        PaymentUser user = findUserByEmail(email).orElseThrow();
        return walletRepository.findWalletByPaymentUserForUpdate(user);
    }

    public Optional<Wallet> findWalletByUserId(Long userId) {
        PaymentUser user = findUserByUserId(userId).orElseThrow();
        return findWalletByUser(user);
    }

    /** 고객 지갑 조회 (비관적 락, 결제 차감 시 동시성 제어용) */
    public Optional<Wallet> findWalletByUserIdForUpdate(Long userId) {
        PaymentUser user = findUserByUserId(userId).orElseThrow();
        return walletRepository.findWalletByPaymentUserForUpdate(user);
    }

    public Optional<Wallet> findSystemWallet() {
        PaymentUser systemUser = paymentUserRepository.findByEmail(CashPolicy.SYSTEM_HOLDING_USER_EMAIL).orElseThrow();
        return walletRepository.findWalletByPaymentUser(systemUser);
    }

    /** 시스템 지갑 조회 (비관적 락, 정산/환불 시 동시성 제어용) */
    public Optional<Wallet> findSystemWalletForUpdate() {
        PaymentUser systemUser = paymentUserRepository.findByEmail(CashPolicy.SYSTEM_HOLDING_USER_EMAIL).orElseThrow();
        return walletRepository.findWalletByPaymentUserForUpdate(systemUser);
    }

    public Optional<Wallet> findPlatformWallet() {
        PaymentUser systemUser = paymentUserRepository.findByEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                .orElseThrow();
        return walletRepository.findWalletByPaymentUser(systemUser);
    }

    /** 플랫폼 지갑 조회 (비관적 락, 정산 시 동시성 제어용) */
    public Optional<Wallet> findPlatformWalletForUpdate() {
        PaymentUser user = paymentUserRepository.findByEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL).orElseThrow();
        return walletRepository.findWalletByPaymentUserForUpdate(user);
    }

    public Long getWalletBalanceByUserId(Long userId) {
        Wallet wallet = findWalletByUserId(userId).orElseThrow();
        return wallet.getBalance();
    }

    public List<CashLog> getCashLogList(Long userId) {
        Wallet wallet = findWalletByUserId(userId).orElseThrow();
        return wallet.getCashLogs();
    }

    public List<Payment> findPaymentListByUserId(Long userId) {
        return paymentRepository.findPaymentsByPaymentUserId(userId);
    }

    public List<Refund> findRefundListByUserId(Long userId) {
        return refundRepository.findRefundsByPayment_PaymentUser_Id(userId);
    }

    public List<CashLog> findSliceCashLogs(Long userId, int page, int size) {
        return cashLogRepository.findCashLogsByPaymentUserIdOrderByIdDesc(userId, PageRequest.of(page, size));
    }

    public void deleteUser(UserResponse user) {
        paymentUserRepository.deleteById(user.id());
    }
}
