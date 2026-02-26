package com.fourtune.auction.boundedContext.auction.adapter.in.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.auction.application.service.AuctionFacade;
import com.fourtune.auction.boundedContext.auction.application.service.AuctionSupport;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.auction.event.AuctionEndingSoonEvent;
import com.fourtune.shared.auction.event.AuctionStartingSoonEvent;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import com.fourtune.auction.infrastructure.kafka.AuctionKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final AuctionKafkaProducer auctionKafkaProducer;
    private final ObjectMapper objectMapper;

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
     * 매 분 0초에 실행 (시작 시각과 맞춰 DB에서 ACTIVE로 전환)
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void startScheduledAuctions() {
        log.info("예정된 경매 시작 작업 시작");

        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            List<AuctionItem> scheduledAuctions = auctionSupport.findScheduledAuctionsToStart(now);

            int startedCount = 0;
            for (AuctionItem auction : scheduledAuctions) {
                try {
                    // ✅ 각 경매마다 독립 트랜잭션으로 처리
                    auctionFacade.startAuctionInTransaction(auction.getId());
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
     * 관심상품 시작 5분 전 알림 발행
     * 매 분 0초마다 실행
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void sendStartingSoonAlerts() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        List<AuctionItem> auctions = auctionSupport.findAuctionsStartingInFiveMinutes(now);

        if (auctions.isEmpty()) {
            return;
        }

        log.info("관심상품 시작 5분 전 알림 대상: {}건", auctions.size());
        for (AuctionItem auction : auctions) {
            try {
                String payload = objectMapper.writeValueAsString(new AuctionStartingSoonEvent(auction.getId(), auction.getTitle()));
                auctionKafkaProducer.send(String.valueOf(auction.getId()), payload,
                        AuctionEventType.AUCTION_STARTING_SOON.name());
                log.debug("AUCTION_STARTING_SOON 발행: auctionId={}", auction.getId());
            } catch (Exception e) {
                log.error("AUCTION_STARTING_SOON 발행 실패: auctionId={}", auction.getId(), e);
            }
        }
    }

    /**
     * 관심상품 종료 5분 전 알림 발행
     * 매 분 30초마다 실행
     */
    @Scheduled(cron = "30 * * * * *", zone = "Asia/Seoul")
    public void sendEndingSoonAlerts() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        List<AuctionItem> auctions = auctionSupport.findAuctionsEndingInFiveMinutes(now);

        if (auctions.isEmpty()) {
            return;
        }

        log.info("관심상품 종료 5분 전 알림 대상: {}건", auctions.size());
        for (AuctionItem auction : auctions) {
            try {
                String payload = objectMapper.writeValueAsString(new AuctionEndingSoonEvent(auction.getId(), auction.getTitle()));
                auctionKafkaProducer.send(String.valueOf(auction.getId()), payload,
                        AuctionEventType.AUCTION_ENDING_SOON.name());
                log.debug("AUCTION_ENDING_SOON 발행: auctionId={}", auction.getId());
            } catch (Exception e) {
                log.error("AUCTION_ENDING_SOON 발행 실패: auctionId={}", auction.getId(), e);
            }
        }
    }

}
