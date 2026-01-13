package com.fourtune.auction.boundedContext.cash.app;

import com.fourtune.auction.boundedContext.cash.domain.CashMember;
import com.fourtune.auction.boundedContext.cash.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CashFacade {

    private final CashSupport cashSupport;

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletBySystem(CashMember System) {
        return cashSupport.findWalletBySystem(System);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletBySystemId(Long systemId) {
        return cashSupport.findWalletBySystemId(systemId);
    }

}
