package com.fourtune.auction.boundedContext.auction.adapter.in.event;

import com.fourtune.auction.boundedContext.auction.application.service.AuctionExtendUseCase;
import com.fourtune.auction.boundedContext.auction.application.service.AuctionSupport;
import com.fourtune.auction.boundedContext.auction.application.service.CartSupport;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.shared.auction.event.AuctionClosedEvent;
import com.fourtune.auction.shared.auction.event.AuctionCreatedEvent;
import com.fourtune.auction.shared.auction.event.BidPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 경매 도메인 이벤트 리스너
 * - 입찰 이벤트 수신 → 자동 연장 체크
 * - 경매 종료 이벤트 수신 → 장바구니 아이템 만료 처리
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AuctionEventListener {

    private final AuctionSupport auctionSupport;
    private final AuctionExtendUseCase auctionExtendUseCase;
    private final CartSupport cartSupport;

    /**
     * 입찰 완료 이벤트 처리
     * - 자동 연장 체크 (BidPlaceUseCase에서 이미 처리하지만, 비동기 추가 처리용)
     * - 실시간 알림 전송 (TODO: NotificationService 연동)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidPlaced(BidPlacedEvent event) {
        log.info("입찰 이벤트 수신: auctionId={}, bidId={}, bidderId={}, amount={}", 
                event.auctionId(), event.bidId(), event.bidderId(), event.bidAmount());
        
        // TODO: 실시간 알림 전송 (WebSocket or SSE)
        // notificationService.notifyBidPlaced(event);
        
        // TODO: 이전 최고 입찰자에게 알림
        // notificationService.notifyOutbid(previousHighestBidderId, event);
    }

    /**
     * 경매 생성 이벤트 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionCreated(AuctionCreatedEvent event) {
        log.info("경매 생성 이벤트 수신: auctionId={}", event.auctionId());
        
        // TODO: Elasticsearch 색인 (SearchService 연동)
        // searchService.indexAuction(event.auctionId());
    }

    /**
     * 경매 종료 이벤트 처리
     * - 장바구니에 담긴 해당 경매 아이템 만료 처리
     */
    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionClosed(AuctionClosedEvent event) {
        log.info("경매 종료 이벤트 수신: auctionId={}, winnerId={}, finalPrice={}", 
                event.auctionId(), event.winnerId(), event.finalPrice());
        
        // 1. 장바구니에 담긴 해당 경매 아이템 만료 처리
        try {
            cartSupport.expireCartItemsByAuctionId(event.auctionId());
            log.debug("장바구니 아이템 만료 처리 완료: auctionId={}", event.auctionId());
        } catch (Exception e) {
            log.error("장바구니 아이템 만료 처리 실패: auctionId={}, error={}", 
                    event.auctionId(), e.getMessage());
        }
        
        // TODO: 낙찰자에게 알림
        // if (event.winnerId() != null) {
        //     notificationService.notifyAuctionWon(event.winnerId(), event);
        // }
        
        // TODO: 판매자에게 알림
        // notificationService.notifyAuctionClosed(sellerId, event);
        
        // TODO: Elasticsearch 업데이트
        // searchService.updateAuctionStatus(event.auctionId());
    }
}
