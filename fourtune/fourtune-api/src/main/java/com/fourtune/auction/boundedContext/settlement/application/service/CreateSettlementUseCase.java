package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import com.fourtune.auction.shared.settlement.dto.SettlementUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CreateSettlementUseCase {

    private final SettlementUserRepository settlementUserRepository;
    private final SettlementRepository settlementRepository;

    /**
     * user(payee)의 settlement 생성
     */
    public Settlement createSettlement(Long payeeId){
        SettlementUser payee = settlementUserRepository.findById(payeeId).orElseThrow();

        Settlement settlement = new Settlement(payee);

        return settlementRepository.save(settlement);
    }

}
