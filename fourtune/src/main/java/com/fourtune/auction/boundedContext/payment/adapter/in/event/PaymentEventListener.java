package com.fourtune.auction.boundedContext.payment.adapter.in.event;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.auction.shared.payment.event.PaymentUserCreatedEvent;
import com.fourtune.auction.shared.settlement.event.SettlementCompletedEvent;
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

    //TODO: User Joined
//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = REQUIRES_NEW)
//    public void handle(UserCreatedEvent event) {
//        paymentFacade.syncUser(evet.getUserDto());
//    }
    //TODO: User Updated
//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = REQUIRES_NEW)
//    public void handle(UserUpdatedEvent event) {
//        paymentFacade.syncUser(event.getUserDto());
//    }

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
