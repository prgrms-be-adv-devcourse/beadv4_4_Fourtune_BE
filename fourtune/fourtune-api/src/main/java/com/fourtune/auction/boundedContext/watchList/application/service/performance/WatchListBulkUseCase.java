package com.fourtune.auction.boundedContext.watchList.application.service.performance;

import com.fourtune.auction.boundedContext.watchList.port.out.WatchListRepository;
import com.fourtune.common.global.eventPublisher.EventPublisher;
import com.fourtune.common.shared.watchList.event.WatchListAuctionStartedEvent;
import com.fourtune.common.shared.watchList.event.WatchListAuctionEndedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DB Bulk 방식 WatchList 서비스
 *
 * 특징:
 * - 조회: 1회 쿼리 (findAllByAuctionItemId)
 * - 업데이트: 1회 Bulk UPDATE (bulkMarkStartAlertSent)
 * - 총 2회 쿼리로 N건 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WatchListBulkUseCase {

    private final WatchListRepository watchListRepository;
    private final EventPublisher eventPublisher;

    /**
     * 경매 시작 알림 처리 (Bulk 방식)
     * 쿼리: 1회 조회 + 1회 Bulk UPDATE = 2회
     */
    @Transactional
    public ProcessResult processAuctionStart(Long auctionItemId) {
        long startTime = System.currentTimeMillis();

        // 1회 쿼리: 관심등록 유저 ID 리스트 조회
        List<Long> userIds = watchListRepository.findAllByAuctionItemId(auctionItemId);

        if (userIds.isEmpty()) {
            return new ProcessResult(0, 0, 0);
        }

        // 이벤트 발행
        eventPublisher.publish(new WatchListAuctionStartedEvent(userIds, auctionItemId));

        // 1회 Bulk UPDATE: 알림 발송 완료 마킹
        int updatedCount = watchListRepository.bulkMarkStartAlertSent(auctionItemId);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[BULK] 경매 {} 시작 알림 처리 완료: {}명, {}ms", auctionItemId, userIds.size(), duration);

        return new ProcessResult(userIds.size(), 2, duration);
    }

    /**
     * 경매 종료 알림 처리 (Bulk 방식)
     */
    @Transactional
    public ProcessResult processAuctionEnd(Long auctionItemId) {
        long startTime = System.currentTimeMillis();

        List<Long> userIds = watchListRepository.findAllByAuctionItemId(auctionItemId);

        if (userIds.isEmpty()) {
            return new ProcessResult(0, 0, 0);
        }

        eventPublisher.publish(new WatchListAuctionEndedEvent(userIds, auctionItemId));
        int updatedCount = watchListRepository.bulkMarkEndAlertSent(auctionItemId);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[BULK] 경매 {} 종료 알림 처리 완료: {}명, {}ms", auctionItemId, userIds.size(), duration);

        return new ProcessResult(userIds.size(), 2, duration);
    }

    /**
     * 처리 결과 DTO
     */
    public record ProcessResult(
            int userCount,
            int queryCount,
            long durationMs
    ) {}
}
