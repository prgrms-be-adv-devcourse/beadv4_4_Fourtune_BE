package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.constant.CashPolicy;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

    public Optional<Wallet> findWalletByUserId(Long userId) {
        return walletRepository.findWalletById(userId);
    }

    public Optional<Wallet> findSystemWallet() {
        return walletRepository.findWalletById(CashPolicy.SYSTEM_MEMBER_ID);
    }
}
