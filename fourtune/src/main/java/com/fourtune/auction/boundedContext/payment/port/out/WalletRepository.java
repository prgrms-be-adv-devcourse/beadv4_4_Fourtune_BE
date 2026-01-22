package com.fourtune.auction.boundedContext.payment.port.out;

import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findWalletById(Long userId);
    Optional<Wallet> findWalletByPaymentUser(PaymentUser paymentUser);
}
