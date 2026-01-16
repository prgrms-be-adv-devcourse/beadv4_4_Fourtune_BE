package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaymentFacade {

    private final PaymentSupport paymentSupport;

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletByUser(PaymentUser paymentUser) {
        return paymentSupport.findWalletByUser(paymentUser);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletByUserId(Long userId) {
        return paymentSupport.findWalletByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findSystemWallet() {
        return paymentSupport.findSystemWallet();
    }

}
