package com.fourtune.auction.boundedContext.cash.out;

import com.fourtune.auction.boundedContext.cash.domain.CashMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashMemberRepository extends JpaRepository<CashMember, Long> {
    Optional<CashMember> findByUsername(String username);
}