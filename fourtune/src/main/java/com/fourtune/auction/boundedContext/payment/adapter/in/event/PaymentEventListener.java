package com.fourtune.auction.boundedContext.payment.adapter.in.event;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.auction.shared.payment.event.PaymentUserCreatedEvent;
import com.fourtune.auction.shared.settlement.event.SettlementCompletedEvent;
import com.fourtune.auction.shared.user.event.UserDeletedEvent;
import com.fourtune.auction.shared.user.event.UserJoinedEvent;
import com.fourtune.auction.shared.user.event.UserModifiedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@RequiredArgsConstructor
@Component
public class PaymentEventListener {
    private final PaymentFacade paymentFacade;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(UserJoinedEvent event) {
        paymentFacade.syncUser(event.getUser());
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(UserModifiedEvent event) {
        paymentFacade.syncUser(event.getUser());
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(UserDeletedEvent event) {
        paymentFacade.deleteUser(event.getUser());
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(PaymentUserCreatedEvent event) {
        paymentFacade.createWallet(event.getPaymentUserDto());
    }

    // 현금처리 예외 발생시 정산까지 롤백
    @EventListener
    public void handle(SettlementCompletedEvent event) {
        paymentFacade.completeSettlement(event.getSettlementDto());
    }

}
