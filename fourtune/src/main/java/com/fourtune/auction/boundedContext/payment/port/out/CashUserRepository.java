package com.fourtune.auction.boundedContext.payment.port.out;

import com.fourtune.auction.boundedContext.payment.domain.entity.CashUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashUserRepository extends JpaRepository<CashUser, Long> {
    Optional<CashUser> findByNickname(String nickname);
    Optional<CashUser> findByEmail(String email);
    Optional<CashUser> findByUserId(Long userId);
}