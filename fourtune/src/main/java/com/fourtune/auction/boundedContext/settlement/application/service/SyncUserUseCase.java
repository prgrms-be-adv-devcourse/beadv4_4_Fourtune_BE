package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.settlement.dto.SettlementUserDto;
import com.fourtune.auction.shared.settlement.event.SettlementUserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SyncUserUseCase {
    private final SettlementUserRepository settlementUserRepository;
    private final EventPublisher eventPublisher;

    public SettlementUser syncUser(SettlementUserDto user) {
        boolean isNew = !settlementUserRepository.existsById(user.getId());

        SettlementUser newUser = settlementUserRepository.save(
                new SettlementUser(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname(),
                        "",
                        user.getPhoneNumber(),
                        user.getCreatedAt(),
                        user.getUpdatedAt(),
                        user.getDeletedAt(),
                        user.getStatus()
                )
        );

        if(isNew){
            eventPublisher.publish(
                    new SettlementUserCreatedEvent(
                            newUser.toDto()
                    )
            );
        }

        return newUser;
    }
}
