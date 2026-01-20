package com.fourtune.auction.boundedContext.settlement.adapter.in.event;

import com.fourtune.auction.boundedContext.settlement.application.service.SettlementFacade;
import com.fourtune.auction.shared.settlement.event.SettlementCompletedEvent;
import com.fourtune.auction.shared.settlement.event.SettlementUserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@RequiredArgsConstructor
@Component
public class SettlementListener {

    private final SettlementFacade settlementFacade;

//    todo: member created, modified -> user sync
//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = REQUIRES_NEW)
//    public void handle(/*user created event*/) {
//        settlementFacade.syncUser();
//    }

//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = REQUIRES_NEW)
//    public void handle(/*user updated event*/) {
//        settlementFacade.syncUser();
//    }

    /**
     * 정산 유저 생성시 빈 정산 생성
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(SettlementUserCreatedEvent event) {
        settlementFacade.createSettlement(event.getSettlementUserDto().getId());
    }

    //todo: payment completed or order completed event -> 정산 후보로 등록
//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = REQUIRES_NEW)
//    public void handle(/*auction order completed event*/) {
//        settlementFacade.createdSettlementCandidatedItem();
//    }

    /**
     * 정산 완료시 새로운 빈 정산 생성
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(SettlementCompletedEvent event) {
        settlementFacade.createSettlement(event.getSettlementDto().getPayeeId());
    }

}
