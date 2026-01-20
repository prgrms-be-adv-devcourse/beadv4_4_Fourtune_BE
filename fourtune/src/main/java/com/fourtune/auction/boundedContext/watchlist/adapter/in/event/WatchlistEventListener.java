package com.fourtune.auction.boundedContext.watchlist.adapter.in.event;

import com.fourtune.auction.boundedContext.notification.application.service.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.constant.NotificationType;
import com.fourtune.auction.boundedContext.watchlist.application.service.WatchlistSupport;
import com.fourtune.auction.boundedContext.watchlist.domain.entity.Watchlist;
import com.fourtune.auction.boundedContext.watchlist.port.out.WatchlistRepository;
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

import java.util.List;

/**
 * 관심상품 이벤트 리스너
 * - 경매 관련 이벤트 수신하여 관심상품 사용자에게 알림
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WatchlistEventListener {

    private final WatchlistSupport watchlistSupport;
    private final WatchlistRepository watchlistRepository;
    private final NotificationFacade notificationFacade;

    /**
     * 입찰 이벤트 처리
     * - 관심상품 등록자에게 가격 변동 알림
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBidPlaced(BidPlacedEvent event) {
        log.debug("관심상품 입찰 이벤트 수신: auctionId={}", event.auctionId());
        
        try {
            // 가격 변동 알림 대상 조회
            List<Watchlist> targets = watchlistSupport.findPriceChangeNotifyTargets(event.auctionId());
            
            for (Watchlist watchlist : targets) {
                // 입찰자 본인에게는 알림 X
                if (watchlist.getUserId().equals(event.bidderId())) {
                    continue;
                }
                
                notificationFacade.sendNotification(
                        watchlist.getUserId(),
                        NotificationType.WATCHLIST_PRICE_DROP,
                        "관심상품 가격 변동",
                        String.format("'%s' 상품의 현재가가 %s원으로 변경되었습니다.", 
                                event.auctionTitle(), event.bidAmount())
                );
            }
        } catch (Exception e) {
            log.error("관심상품 가격 변동 알림 실패: auctionId={}", event.auctionId(), e);
        }
    }

    /**
     * 경매 종료 이벤트 처리
     * - 관심상품 등록자에게 경매 종료 알림
     * - 관심상품 데이터 정리 (선택적)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAuctionClosed(AuctionClosedEvent event) {
        log.debug("관심상품 경매 종료 이벤트 수신: auctionId={}", event.auctionId());
        
        try {
            // 관심상품 등록자 조회
            List<Watchlist> watchlists = watchlistSupport.findByAuctionId(event.auctionId());
            
            for (Watchlist watchlist : watchlists) {
                // 낙찰자에게는 별도 알림 (AuctionEventListener에서 처리)
                if (event.winnerId() != null && watchlist.getUserId().equals(event.winnerId())) {
                    continue;
                }
                
                String message = event.winnerId() != null 
                        ? String.format("'%s' 경매가 %s원에 낙찰되었습니다.", event.auctionTitle(), event.finalPrice())
                        : String.format("'%s' 경매가 입찰 없이 종료되었습니다.", event.auctionTitle());
                
                notificationFacade.sendNotification(
                        watchlist.getUserId(),
                        NotificationType.AUCTION_ENDED,
                        "관심상품 경매 종료",
                        message
                );
            }
            
            // 종료된 경매의 관심상품 데이터 삭제 (선택적)
            // watchlistRepository.deleteByAuctionId(event.auctionId());
            
        } catch (Exception e) {
            log.error("관심상품 경매 종료 알림 실패: auctionId={}", event.auctionId(), e);
        }
    }

}
