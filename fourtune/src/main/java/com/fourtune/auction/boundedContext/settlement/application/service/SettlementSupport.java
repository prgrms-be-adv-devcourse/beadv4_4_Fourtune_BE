package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementCandidatedItemRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SettlementSupport {

    private final SettlementUserRepository settlementUserRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementCandidatedItemRepository settlementCandidatedItemRepository;

    public Optional<SettlementUser> findSystemHoldingUser(){
        return settlementUserRepository.findByEmail("holding@system.com");
    }

    public Optional<SettlementUser> findPlatformRevenueUser(){
        return settlementUserRepository.findByEmail("revenue@platform.com");
    }

    public Optional<SettlementUser> findUserById(Long userId){
        return settlementUserRepository.findById(userId);
    }

    public List<Settlement> findLatestSettlementByUserId(Long userId){
        return settlementRepository.findFirstByPayeeIdAndSettledAtIsNotNullOrderByCreatedAtDesc(userId);
    }

    public List<Settlement> findAllByPayeeIdOrderBySettledAtDesc(Long userId){
        return settlementRepository.findSettlementsByPayee_IdOrderBySettledAtDesc(userId);
    }


    public List<SettlementCandidatedItem> findSettlementCandidatedItems(Long payeeId) {
        return settlementCandidatedItemRepository.findByPayee_Id(payeeId);
    }

    public void deleteUserById(Long id) {
        settlementUserRepository.deleteById(id);
    }
}
