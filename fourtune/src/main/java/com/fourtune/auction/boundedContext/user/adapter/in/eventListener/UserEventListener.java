package com.fourtune.auction.boundedContext.user.adapter.in.eventListener;

import com.fourtune.auction.boundedContext.user.application.service.UserModifiedUseCase;
import com.fourtune.auction.shared.auction.event.AuctionPenaltyEvent;
import com.fourtune.auction.shared.auction.event.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 유저 도메인 이벤트 수신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserModifiedUseCase userModifiedUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPenaltyEvent(OrderCancelledEvent event) {
        userModifiedUseCase.penalty(event.userId());
    }
}
