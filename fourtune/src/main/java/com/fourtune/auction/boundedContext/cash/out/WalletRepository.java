package com.fourtune.auction.boundedContext.cash.out;

import com.fourtune.auction.boundedContext.cash.domain.CashUser;
import com.fourtune.auction.boundedContext.cash.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    @Query("select w from Wallet w where w.user = :system")
    Optional<Wallet> findBySystem(@Param("system") CashUser system);

    @Query("select w from Wallet w where w.user.id = :systemId")
    Optional<Wallet> findBySystemId(@Param("systemId") Long systemId);
}