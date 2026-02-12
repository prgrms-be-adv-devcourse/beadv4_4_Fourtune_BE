package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.adapter.in.web.dto.SettlementCandidatedItemDto;
import com.fourtune.auction.boundedContext.settlement.adapter.in.web.dto.SettlementResponse;
import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.common.shared.payment.dto.OrderDto;
import com.fourtune.common.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional(readOnly = true)
    public Optional<SettlementUser> findSystemHoldingUser(){
        return settlementSupport.findSystemHoldingUser();
    }

    @Transactional(readOnly = true)
    public Optional<SettlementUser> findPlatformRevenueUser(){
        return settlementSupport.findPlatformRevenueUser();
    }

    @Transactional(readOnly = true)
    public Optional<SettlementUser> findUserById(Long userId){
        return settlementSupport.findUserById(userId);
    }

    @Transactional
    public Settlement createSettlement(Long userId){
        return createSettlementUseCase.createSettlement(userId);
    }

    @Transactional
    public SettlementUser syncUser(UserResponse userResponse){
        return syncUserUseCase.syncUser(userResponse);
    }

    @Transactional(readOnly = true)
    public SettlementResponse findLatestSettlementByUserId(Long userId){
        Settlement settlement = settlementSupport.findLatestSettlementByUserId(userId).getFirst();
        return settlement.toResponse();
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> findAllSettlementsByPayeeId(Long userId){
        List<Settlement> settlements = settlementSupport.findAllByPayeeIdOrderBySettledAtDesc(userId);
        return settlements.stream()
                .map(Settlement::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addSettlementCandidatedItem(OrderDto orderDto){
        addSettlementCandidatedItemsUseCase.addSettlementCandidatedItems(orderDto);
    }

    @Transactional
    public int collectSettlementItemChunk(int size){
        return collectSettlementItemChunkUseCase.collectSettlementItemChunk(size);
    }

    @Transactional
    public int completeSettlementsChunk(int size){
        return completeSettlementChunkUseCase.completeSettlementsChunk(size);
    }

    @Transactional(readOnly = true)
    public List<SettlementCandidatedItemDto> findSettlementCandidatedItems(Long payeeId) {

        List<SettlementCandidatedItem> pendingsOfPayee = settlementSupport.findSettlementCandidatedItems(payeeId);

        List<SettlementCandidatedItemDto> dtos = pendingsOfPayee.stream()
                .map(SettlementCandidatedItemDto::new)
                .collect(Collectors.toList());

        return dtos;
    }

    @Transactional
    public void deleteUser(UserResponse user) {
        settlementSupport.deleteUserById(user.id());
    }
}
