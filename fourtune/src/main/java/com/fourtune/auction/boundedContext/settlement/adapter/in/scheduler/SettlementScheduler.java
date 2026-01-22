package com.fourtune.auction.boundedContext.settlement.adapter.in.scheduler;

import com.fourtune.auction.boundedContext.settlement.application.service.SettlementFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@EnableScheduling
public class SettlementScheduler {

    private final SettlementFacade settlementFacade;

    // [Collect] 매일 새벽 4시
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void runCollectItems() {
        log.info("정산 데이터 수집 작업 시작 (Daily Collect)");

        int size = 10;
        int count = size;

        while(count >= size){
            count = settlementFacade.collectSettlementItemChunk(size);
            log.info("Collect Chunk 처리: {}건", count);
        }

        log.info("정산 데이터 수집 작업 종료");
    }

    // [Complete] 매월 1일 오전 9시: 모아둔 돈 한방에 지급
    // "0 0 9 1 * *" -> 매월 1일 09:00:00
    @Scheduled(cron = "0 0 9 1 * *", zone = "Asia/Seoul")
    public void runCompleteItems() {
        log.info("정산 금액 지급 작업 시작 (Monthly Complete)");

        int size = 10;
        int count = size;
        
        while(count >= size){
            count = settlementFacade.completeSettlementsChunk(size);
            log.info("Complete Chunk 처리: {}건", count);
        }

        log.info("정산 금액 지급 작업 종료");
    }
}
