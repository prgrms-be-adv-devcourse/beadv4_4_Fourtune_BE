package com.fourtune.auction.boundedContext.cash.out;

import com.fourtune.auction.boundedContext.cash.domain.CashMember;
import com.fourtune.auction.boundedContext.cash.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findBySystem(CashMember system);

    Optional<Wallet> findBySystemId(Long systemId);
}
