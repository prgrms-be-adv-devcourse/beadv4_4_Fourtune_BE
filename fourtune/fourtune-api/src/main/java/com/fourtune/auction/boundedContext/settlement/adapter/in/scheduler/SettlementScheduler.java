package com.fourtune.auction.boundedContext.settlement.adapter.in.scheduler;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobOperator jobOperator;

    @Qualifier("settlementCollectJob")
    private final Job settlementCollectJob;

    @Qualifier("settlementCompleteJob")
    private final Job settlementCompleteJob;

    @Scheduled(cron = "${settlement.scheduler.collect-cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void runCollectItems() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobOperator.start(settlementCollectJob, params);
        } catch (Exception e) {
            log.error("정산 수집 배치 실행 실패: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${settlement.scheduler.complete-cron:0 0 9 1 * *}", zone = "Asia/Seoul")
    public void runCompleteItems() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobOperator.start(settlementCompleteJob, params);
        } catch (Exception e) {
            log.error("정산 완료 배치 실행 실패: {}", e.getMessage(), e);
        }
    }
}