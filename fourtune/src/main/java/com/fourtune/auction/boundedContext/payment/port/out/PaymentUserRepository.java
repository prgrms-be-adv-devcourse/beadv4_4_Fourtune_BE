package com.fourtune.auction.boundedContext.payment.port.out;

import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentUserRepository extends JpaRepository<PaymentUser, Long> {
    Optional<PaymentUser> findByNickname(String nickname);
    Optional<PaymentUser> findByEmail(String email);
    Optional<PaymentUser> findByUserId(Long userId);
}