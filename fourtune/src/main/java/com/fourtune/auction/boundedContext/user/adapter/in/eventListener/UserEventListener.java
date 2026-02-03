package com.fourtune.auction.boundedContext.user.adapter.in.eventListener;

import com.fourtune.auction.boundedContext.user.application.service.UserModifiedUseCase;
import com.fourtune.auction.shared.auction.event.AuctionPenaltyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserModifiedUseCase userModifiedUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPenaltyEvent(AuctionPenaltyEvent event){
        userModifiedUseCase.penalty(event.userId());
    }


}
