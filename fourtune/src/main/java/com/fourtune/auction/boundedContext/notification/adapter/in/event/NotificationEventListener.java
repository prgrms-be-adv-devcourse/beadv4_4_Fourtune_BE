package com.fourtune.auction.boundedContext.notification.adapter.in.event;

import com.fourtune.auction.boundedContext.notification.application.service.NotificationFacade;
import com.fourtune.auction.shared.auction.event.AuctionClosedEvent;
import com.fourtune.auction.shared.auction.event.BidPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 알림 이벤트 리스너
 * - 다른 도메인의 이벤트를 수신하여 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationFacade notificationFacade;

    /**
     * 입찰 완료 이벤트 처리
     * - 판매자에게 입찰 알림 발송
     * - 이전 최고 입찰자에게 상위 입찰 알림 발송
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBidPlaced(BidPlacedEvent event) {
        log.info("입찰 이벤트 수신: auctionId={}, bidderId={}", event.auctionId(), event.bidderId());
        
        try {
            // 1. 판매자에게 알림
            notificationFacade.sendBidNotification(
                    event.sellerId(),
                    event.auctionId(),
                    event.auctionTitle(),
                    "입찰자"  // TODO: 실제 입찰자 닉네임 추가
            );
            
            // 2. 이전 최고 입찰자에게 알림 (있는 경우)
            if (event.previousBidderId() != null && !event.previousBidderId().equals(event.bidderId())) {
                notificationFacade.sendOutbidNotification(
                        event.previousBidderId(),
                        event.auctionId(),
                        event.auctionTitle()
                );
            }
        } catch (Exception e) {
            log.error("입찰 알림 발송 실패: auctionId={}", event.auctionId(), e);
        }
    }

    /**
     * 경매 종료 이벤트 처리
     * - 낙찰자에게 낙찰 알림 발송
     * - 판매자에게 경매 종료 알림 발송
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAuctionClosed(AuctionClosedEvent event) {
        log.info("경매 종료 이벤트 수신: auctionId={}, winnerId={}", event.auctionId(), event.winnerId());
        
        try {
            // 1. 판매자에게 경매 종료 알림
            notificationFacade.sendAuctionEndedNotification(
                    event.sellerId(),
                    event.auctionId(),
                    event.auctionTitle(),
                    event.winnerId() != null
            );
            
            // 2. 낙찰자에게 낙찰 알림 (있는 경우)
            if (event.winnerId() != null) {
                notificationFacade.sendWinNotification(
                        event.winnerId(),
                        event.auctionId(),
                        event.auctionTitle()
                );
            }
        } catch (Exception e) {
            log.error("경매 종료 알림 발송 실패: auctionId={}", event.auctionId(), e);
        }
    }

}
