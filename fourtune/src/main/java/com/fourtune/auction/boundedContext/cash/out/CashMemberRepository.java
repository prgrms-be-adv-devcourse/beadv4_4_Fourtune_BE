package com.fourtune.auction.boundedContext.cash.out;

import com.fourtune.auction.boundedContext.cash.domain.CashMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashMemberRepository extends JpaRepository<CashMember, Long> {
}
