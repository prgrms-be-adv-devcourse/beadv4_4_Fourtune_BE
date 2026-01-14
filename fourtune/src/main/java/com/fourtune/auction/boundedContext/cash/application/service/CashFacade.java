package com.fourtune.auction.boundedContext.cash.application.service;

import com.fourtune.auction.boundedContext.cash.domain.entity.CashUser;
import com.fourtune.auction.boundedContext.cash.domain.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CashFacade {

    private final CashSupport cashSupport;

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletBySystem(CashUser System) {
        return cashSupport.findWalletBySystem(System);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletBySystemId(Long systemId) {
        return cashSupport.findWalletBySystemId(systemId);
    }

}
