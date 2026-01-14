package com.fourtune.auction.boundedContext.cash.application.service;

import com.fourtune.auction.boundedContext.cash.domain.entity.CashUser;
import com.fourtune.auction.boundedContext.cash.domain.constant.CashPolicy;
import com.fourtune.auction.boundedContext.cash.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.cash.port.out.CashUserRepository;
import com.fourtune.auction.boundedContext.cash.port.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CashSupport {
    private final CashUserRepository cashUserRepository;
    private final WalletRepository walletRepository;

    public Optional<CashUser> findMemberByEmail(String email) {
        return cashUserRepository.findByEmail(email);
    }

    public Optional<Wallet> findWalletBySystem(CashUser system) {
        return walletRepository.findBySystem(system);
    }

    public Optional<Wallet> findWalletBySystemId(Long systemId) {
        return walletRepository.findBySystemId(systemId);
    }

    public Optional<Wallet> findSystemWallet() {
        return walletRepository.findBySystemId(CashPolicy.SYSTEM_MEMBER_ID);
    }
}
