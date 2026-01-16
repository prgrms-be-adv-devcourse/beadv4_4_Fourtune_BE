package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.CashUser;
import com.fourtune.auction.boundedContext.payment.domain.constant.CashPolicy;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.CashUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CashSupport {
    private final CashUserRepository cashUserRepository;
    private final WalletRepository walletRepository;

    public Optional<CashUser> findUserByEmail(String email) {
        return cashUserRepository.findByEmail(email);
    }
    public Optional<CashUser> findUserByUserId(Long userId) {
        return cashUserRepository.findById(userId);
    }

    public Optional<Wallet> findWalletByUser(CashUser user) {
        return walletRepository.findWalletByUser(user);
    }

    public Optional<Wallet> findWalletByUserId(Long userId) {
        return walletRepository.findWalletByUserId(userId);
    }

    public Optional<Wallet> findSystemWallet() {
        return walletRepository.findWalletByUserId(CashPolicy.SYSTEM_MEMBER_ID);
    }
}
