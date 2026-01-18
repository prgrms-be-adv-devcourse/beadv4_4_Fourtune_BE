package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 입찰 등록 UseCase
 * - 입찰 처리
 * - 동시성 제어 (분산 락)
 * - 자동 연장 체크
 * - 실시간 알림
 */
@Service
@RequiredArgsConstructor
public class BidPlaceUseCase {

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;
    private final AuctionExtendUseCase auctionExtendUseCase;
    // private final RedisLockService redisLockService; // TODO: 나중에 추가
    // private final NotificationService notificationService; // TODO: 나중에 추가

    /**
     * 입찰 등록
     */
    @Transactional
    public Long placeBid(Long auctionId, Long bidderId, BigDecimal bidAmount) {
        // TODO: 구현 필요 (분산 락 적용)
        // 1. 경매 조회
        // 2. 입찰 가능 여부 확인
        //    - 경매가 ACTIVE 상태인지
        //    - 본인이 판매자가 아닌지
        //    - 입찰 금액이 유효한지 (현재가 + 입찰단위)
        // 3. 이전 최고가 입찰자의 입찰 상태 변경 (ACTIVE -> FAILED)
        // 4. 새 입찰 생성 및 저장
        // 5. 경매의 currentPrice 업데이트
        // 6. 자동 연장 체크 (종료 5분 전이면 연장)
        // 7. 실시간 알림 (WebSocket or SSE)
        // 8. 입찰 ID 반환
        return null;
    }

    /**
     * 입찰 가능 여부 검증
     */
    private void validateBidPlaceable(AuctionItem auctionItem, Long bidderId, BigDecimal bidAmount) {
        // TODO: 구현 필요
        // - 경매가 ACTIVE 상태인지
        // - 본인이 판매자가 아닌지
        // - 입찰 금액이 현재가 + 입찰단위 이상인지
        // - 경매 종료 시간이 지나지 않았는지
    }

}
