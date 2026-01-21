package com.fourtune.auction.boundedContext.settlement.port.out;

import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementUserRepository extends JpaRepository<SettlementUser, Long> {
    Optional<SettlementUser> findByEmail(String email);
}
