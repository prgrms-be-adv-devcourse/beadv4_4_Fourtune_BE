package com.fourtune.auction.boundedContext.settlement.adapter.in.event;

import com.fourtune.auction.boundedContext.settlement.application.service.SettlementFacade;
import com.fourtune.auction.shared.auction.event.OrderCompletedEvent;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import com.fourtune.auction.shared.settlement.event.SettlementCompletedEvent;
import com.fourtune.auction.shared.settlement.event.SettlementUserCreatedEvent;
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
public class SettlementListener {

    private final SettlementFacade settlementFacade;

    /**
     * [수정] fallbackExecution = true 추가
     * 트랜잭션이 없는 곳(InitData 등)에서 발행된 이벤트도 처리하기 위함
     */
    @TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(UserJoinedEvent event) {
        log.info("[SettlementListener] UserJoinedEvent 수신: {}", event.getUser().email());
        settlementFacade.syncUser(event.getUser());
    }

    @TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(UserModifiedEvent event) {
        settlementFacade.syncUser(event.getUser());
    }

    @TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(UserDeletedEvent event) {
        settlementFacade.deleteUser(event.getUser());
    }

    /**
     * 정산 유저 생성시 빈 정산 생성
     * 주의: settlementFacade.syncUser() 내부에서 SettlementUserCreatedEvent를 발행해야 이 메서드가 실행됩니다.
     */
    @TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(SettlementUserCreatedEvent event) {
        log.info("[SettlementListener] 정산 유저 생성 확인 -> 초기 정산서 생성: ID={}", event.getSettlementUserDto().getId());
        settlementFacade.createSettlement(event.getSettlementUserDto().getId());
    }

    /**
     * 정산 완료시 새로운 빈 정산 생성
     */
    @TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(SettlementCompletedEvent event) {
        log.info("[SettlementListener] 정산 완료 확인 -> 새 정산서 생성: PayeeID={}", event.getSettlementDto().getPayeeId());
        settlementFacade.createSettlement(event.getSettlementDto().getPayeeId());
    }

    /**
     * 주문 완료시 정산 후보 등록
     */
    @TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(OrderCompletedEvent event) {
        log.info("[SettlementListener] 주문 완료 확인 ={}", event.orderId());

        OrderDto orderDto = OrderDto.from(event);

        settlementFacade.addSettlementCandidatedItem(orderDto);
    }
}