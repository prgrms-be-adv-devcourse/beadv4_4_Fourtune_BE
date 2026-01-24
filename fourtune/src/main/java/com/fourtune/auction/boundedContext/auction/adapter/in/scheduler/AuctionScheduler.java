package com.fourtune.auction.boundedContext.auction.adapter.in.scheduler;

import com.fourtune.auction.boundedContext.auction.application.service.AuctionFacade;
import com.fourtune.auction.boundedContext.auction.application.service.AuctionSupport;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.event.AuctionStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 경매 스케줄러
 * - 종료 시간이 지난 경매 자동 종료
 * - 시작 시간이 된 경매 자동 시작
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AuctionScheduler {

    private final AuctionFacade auctionFacade;
    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;

    /**
     * 만료된 경매 종료 처리
     * 매 분마다 실행
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void closeExpiredAuctions() {
        log.info("만료된 경매 종료 작업 시작");
        
        try {
            auctionFacade.closeExpiredAuctions();
            log.info("만료된 경매 종료 작업 완료");
        } catch (Exception e) {
            log.error("만료된 경매 종료 작업 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 예정된 경매 시작 처리
     * 매 분마다 실행
     */
    @Scheduled(cron = "30 * * * * *", zone = "Asia/Seoul")
    public void startScheduledAuctions() {
        log.info("예정된 경매 시작 작업 시작");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<AuctionItem> scheduledAuctions = auctionSupport.findScheduledAuctionsToStart(now);
            
            int startedCount = 0;
            for (AuctionItem auction : scheduledAuctions) {
                try {
                    // ✅ 각 경매마다 독립 트랜잭션으로 처리
                    startAuctionInTransaction(auction.getId());
                    startedCount++;
                    log.debug("경매 ID {} 시작 처리 완료", auction.getId());
                } catch (Exception e) {
                    log.error("경매 ID {} 시작 처리 실패: {}", auction.getId(), e.getMessage(), e);
                }
            }
            
            log.info("예정된 경매 시작 작업 완료: {}건 처리", startedCount);
        } catch (Exception e) {
            log.error("예정된 경매 시작 작업 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 경매 시작 처리 (독립 트랜잭션)
     * 각 경매마다 독립적인 트랜잭션으로 처리하여 하나 실패 시 다른 것들에 영향 없도록 함
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void startAuctionInTransaction(Long auctionId) {
        AuctionItem auction = auctionSupport.findByIdOrThrow(auctionId);
        auction.start();
        auctionSupport.save(auction);
        
        // 경매 시작 이벤트 발행 (관심상품 알림용)
        eventPublisher.publish(new AuctionStartedEvent(
                auction.getId(),
                auction.getTitle(),
                auction.getSellerId(),
                auction.getStartPrice(),
                auction.getAuctionEndTime()
        ));
    }
}
