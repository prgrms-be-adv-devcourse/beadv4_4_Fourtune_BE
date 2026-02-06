package com.fourtune.auction.boundedContext.payment.adapter.in.event;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.auction.global.config.EventPublishingConfig;
import com.fourtune.auction.shared.payment.event.PaymentUserCreatedEvent;
import com.fourtune.auction.shared.settlement.event.SettlementCompletedEvent;
import com.fourtune.auction.shared.user.event.UserDeletedEvent;
import com.fourtune.auction.shared.user.event.UserJoinedEvent;
import com.fourtune.auction.shared.user.event.UserModifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentEventListener {
    private final PaymentFacade paymentFacade;
    private final EventPublishingConfig eventPublishingConfig;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(UserJoinedEvent event) {
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            log.debug("[Payment] User 이벤트는 Kafka로 처리됨 - Spring Event 무시");
            return;
        }
        paymentFacade.syncUser(event.getUser());
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(UserModifiedEvent event) {
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            return;
        }
        paymentFacade.syncUser(event.getUser());
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(UserDeletedEvent event) {
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            return;
        }
        paymentFacade.deleteUser(event.getUser());
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(PaymentUserCreatedEvent event) {
        paymentFacade.createWallet(event.getPaymentUserDto());
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(SettlementCompletedEvent event) {
        paymentFacade.completeSettlement(event.getSettlementDto());
    }

}
