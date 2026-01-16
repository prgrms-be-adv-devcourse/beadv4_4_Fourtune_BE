package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.CashUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CashFacade {

    private final CashSupport cashSupport;

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletByUser(CashUser user) {
        return cashSupport.findWalletByUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findSystemWallet() {
        return cashSupport.findSystemWallet();
    }

}
