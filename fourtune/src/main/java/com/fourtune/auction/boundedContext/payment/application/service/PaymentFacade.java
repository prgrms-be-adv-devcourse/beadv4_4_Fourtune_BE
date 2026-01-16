package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.User;
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
    public Optional<Wallet> findWalletByUser(User user) {
        return paymentSupport.findWalletByUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findSystemWallet() {
        return paymentSupport.findSystemWallet();
    }

}
