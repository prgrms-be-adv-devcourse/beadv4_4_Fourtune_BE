package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SettlementSupport {

    private final SettlementUserRepository settlementUserRepository;

    public Optional<SettlementUser> findSystemHoldingUser(){
        return settlementUserRepository.findByEmail("holding@system.com");
    }

    public Optional<SettlementUser> findPlatformRevenueUser(){
        return settlementUserRepository.findByEmail("revenue@platform.com");
    }

    public Optional<SettlementUser> findUserById(Long userId){
        return settlementUserRepository.findById(userId);
    }

}
