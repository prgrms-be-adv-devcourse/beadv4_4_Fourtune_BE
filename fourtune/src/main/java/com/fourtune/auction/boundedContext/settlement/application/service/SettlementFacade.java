package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import com.fourtune.auction.shared.settlement.dto.SettlementUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SettlementFacade {

    private final SettlementSupport settlementSupport;
    private final CreateSettlementUseCase createSettlementUseCase;
    private final SyncUserUseCase syncUserUseCase;
    private final AddSettlementCandidatedItemsUseCase addSettlementCandidatedItemsUseCase;
    private final CollectSettlementItemChunkUseCase collectSettlementItemChunkUseCase;
    private final CompleteSettlementChunkUseCase completeSettlementChunkUseCase;

    public Optional<SettlementUser> findSystemHoldingUser(){
        return settlementSupport.findSystemHoldingUser();
    }

    public Optional<SettlementUser> findPlatformRevenueUser(){
        return settlementSupport.findPlatformRevenueUser();
    }

    public Optional<SettlementUser> findUserById(Long userId){
        return settlementSupport.findUserById(userId);
    }

    public Settlement createSettlement(Long userId){
        return createSettlementUseCase.createSettlement(userId);
    }

    public SettlementUser syncUser(SettlementUserDto dto){
        return syncUserUseCase.syncUser(dto);
    }

    public Settlement findLatestSettlementByUserId(Long userId){
        return settlementSupport.findLatestSettlementByUserId(userId).getFirst();
    }

    @Transactional
    public void addSettlementCandidatedItem(OrderDto dto){
        addSettlementCandidatedItemsUseCase.addSettlementCandidatedItems(dto);
    }

    @Transactional
    public int collectSettlementItemChunk(int size){
        return collectSettlementItemChunkUseCase.collectSettlementItemChunk(size);
    }

    @Transactional
    public int completeSettlementsChunk(int size){
        return completeSettlementChunkUseCase.completeSettlementsChunk(size);
    }
}
