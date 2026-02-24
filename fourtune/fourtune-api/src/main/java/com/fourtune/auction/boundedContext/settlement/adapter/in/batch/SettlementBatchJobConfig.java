package com.fourtune.auction.boundedContext.settlement.adapter.in.batch;

import com.fourtune.auction.boundedContext.settlement.application.service.SettlementCollectItemChunkUseCase;
import com.fourtune.auction.boundedContext.settlement.application.service.SettlementCompleteChunkUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SettlementCollectItemChunkUseCase collectUseCase;
    private final SettlementCompleteChunkUseCase completeUseCase;

    private static final int CHUNK_SIZE = 10;

    // ==================== Collect Job ====================

    @Bean
    public Job settlementCollectJob(Step settlementCollectStep) {
        return new JobBuilder("settlementCollectJob", jobRepository)
                .start(settlementCollectStep)
                .build();
    }

    @Bean
    public Step settlementCollectStep() {
        return new StepBuilder("settlementCollectStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("정산 데이터 수집 작업 시작");
                    int size = CHUNK_SIZE;
                    int count = size;

                    while (count >= size) {
                        count = collectUseCase.collectSettlementItemChunk(size);
                        log.info("Collect Chunk 처리: {}건", count);
                    }

                    log.info("정산 데이터 수집 작업 종료");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // ==================== Complete Job ====================

    @Bean
    public Job settlementCompleteJob(Step settlementCompleteStep) {
        return new JobBuilder("settlementCompleteJob", jobRepository)
                .start(settlementCompleteStep)
                .build();
    }

    @Bean
    public Step settlementCompleteStep() {
        return new StepBuilder("settlementCompleteStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("정산 금액 지급 작업 시작");
                    int size = CHUNK_SIZE;
                    int count = size;

                    while (count >= size) {
                        count = completeUseCase.completeSettlementsChunk(size);
                        log.info("Complete Chunk 처리: {}건", count);
                    }

                    log.info("정산 금액 지급 작업 종료");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}