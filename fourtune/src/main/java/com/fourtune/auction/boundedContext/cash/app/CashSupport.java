package com.fourtune.auction.boundedContext.cash.app;

import com.fourtune.auction.boundedContext.cash.domain.CashMember;
import com.fourtune.auction.boundedContext.cash.domain.CashPolicy;
import com.fourtune.auction.boundedContext.cash.domain.Wallet;
import com.fourtune.auction.boundedContext.cash.out.CashMemberRepository;
import com.fourtune.auction.boundedContext.cash.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CashSupport {
    private final CashMemberRepository cashMemberRepository;
    private final WalletRepository walletRepository;

    public Optional<CashMember> findMemberByUsername(String username) {
        return cashMemberRepository.findByUsername(username);
    }

    public Optional<Wallet> findWalletBySystem(CashMember system) {
        return walletRepository.findBySystem(system);
    }

    public Optional<Wallet> findWalletBySystemId(Long systemId) {
        return walletRepository.findBySystemId(systemId);
    }

    public Optional<Wallet> findSystemWallet() {
        return walletRepository.findBySystemId(CashPolicy.SYSTEM_MEMBER_ID);
    }
}
