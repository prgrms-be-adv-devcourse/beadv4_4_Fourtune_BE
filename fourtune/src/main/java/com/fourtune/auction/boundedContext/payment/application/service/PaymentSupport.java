package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.CashLog;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.constant.CashPolicy;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentSupport {
    private final PaymentUserRepository paymentUserRepository;
    private final WalletRepository walletRepository;

    public Optional<PaymentUser> findUserByEmail(String email) {
        return paymentUserRepository.findByEmail(email);
    }
    public Optional<PaymentUser> findUserByUserId(Long userId) {
        return paymentUserRepository.findById(userId);
    }

    public Optional<Wallet> findWalletByUser(PaymentUser paymentUser) {
        return walletRepository.findWalletByPaymentUser(paymentUser);
    }

    public Optional<Wallet> findWalletByUserEmail(String email){
        PaymentUser user = findUserByEmail(email).orElseThrow();
        return findWalletByUser(user);
    }

    public Optional<Wallet> findWalletByUserId(Long userId) {
        PaymentUser user = findUserByUserId(userId).orElseThrow();
        return findWalletByUser(user);
    }

    public Optional<Wallet> findSystemWallet() {
        PaymentUser systemUser = paymentUserRepository.findByEmail(CashPolicy.SYSTEM_HOLDING_USER_EMAIL).orElseThrow();
        return walletRepository.findWalletByPaymentUser(systemUser);
    }

    public Optional<Wallet> findPlatformWallet() {
        PaymentUser systemUser = paymentUserRepository.findByEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL).orElseThrow();
        return walletRepository.findWalletByPaymentUser(systemUser);
    }

    public Long getWalletBalanceByUserId(Long userId) {
        Wallet wallet = findWalletByUserId(userId).orElseThrow();
        return wallet.getBalance();
    }

    public List<CashLog> getCashLogList(Long userId) {
        Wallet wallet = findWalletByUserId(userId).orElseThrow();
        return wallet.getCashLogs();
    }
}
