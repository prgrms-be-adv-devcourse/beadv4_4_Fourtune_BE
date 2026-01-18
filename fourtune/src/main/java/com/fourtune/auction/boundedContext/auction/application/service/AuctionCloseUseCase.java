package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.event.AuctionClosedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 경매 종료 UseCase
 * - 경매 시간 종료 시 처리
 * - 낙찰자 결정
 * - Order 생성
 * - 실패한 입찰들 처리
 */
@Service
@RequiredArgsConstructor
public class AuctionCloseUseCase {

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;
    private final OrderCreateUseCase orderCreateUseCase;
    private final EventPublisher eventPublisher;

    /**
     * 경매 종료 처리
     */
    @Transactional
    public void closeAuction(Long auctionId) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 종료 가능 여부 확인 (이미 종료되었는지)
        // 3. 최고가 입찰 조회
        // 4. 낙찰자가 있으면:
        //    - 경매 상태 변경 (ENDED -> SOLD)
        //    - Order 생성 (OrderCreateUseCase 호출)
        //    - 실패한 입찰들 처리 (BidSupport.failAllActiveBids)
        // 5. 낙찰자가 없으면:
        //    - 경매 상태 변경 (ENDED)
        // 6. 이벤트 발행 (AuctionClosedEvent)
    }

    /**
     * 종료 가능 여부 검증
     */
    private void validateCloseable(AuctionItem auctionItem) {
        // TODO: 구현 필요
        // - ACTIVE 상태인지 확인
        // - 종료 시간이 지났는지 확인
    }

}
