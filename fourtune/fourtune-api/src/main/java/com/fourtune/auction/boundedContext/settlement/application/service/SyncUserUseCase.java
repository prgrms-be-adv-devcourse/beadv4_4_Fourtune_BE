package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.settlement.dto.SettlementUserDto;
import com.fourtune.auction.shared.settlement.event.SettlementUserCreatedEvent;
import com.fourtune.auction.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SyncUserUseCase {
    private final SettlementUserRepository settlementUserRepository;
    private final EventPublisher eventPublisher;

    public SettlementUser syncUser(UserResponse user) {
        boolean isNew = !settlementUserRepository.existsById(user.id());

        SettlementUser newUser = settlementUserRepository.save(
                new SettlementUser(
                        user.id(),
                        user.email(),
                        user.nickname(),
                        "",
                        "",
                        user.createdAt(),
                        user.updatedAt(),
                        null,
                        user.status()
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
