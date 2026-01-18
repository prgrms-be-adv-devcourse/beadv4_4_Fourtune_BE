package com.fourtune.auction.boundedContext.auction.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 입찰 Facade
 * - 여러 UseCase를 조합하여 복잡한 비즈니스 플로우 처리
 * - Controller는 이 Facade만 호출
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BidFacade {

    private final BidPlaceUseCase bidPlaceUseCase;
    private final BidCancelUseCase bidCancelUseCase;
    private final BidQueryUseCase bidQueryUseCase;
    private final AuctionExtendUseCase auctionExtendUseCase;

    /**
     * 입찰하기
     */
    public Long placeBid(Long auctionId, Long bidderId, BigDecimal bidAmount) {
        // TODO: 구현 필요
        // 1. BidPlaceUseCase 호출 (입찰 등록)
        // 2. 자동 연장 체크 (AuctionExtendUseCase)
        // 3. 실시간 알림 전송 (WebSocket or SSE)
        // 4. 입찰 ID 반환
        return null;
    }

    /**
     * 입찰 취소
     */
    public void cancelBid(Long bidId, Long bidderId) {
        // TODO: 구현 필요
        return;
    }

    /**
     * 경매의 입찰 내역 조회
     */
    @Transactional(readOnly = true)
    public List<Object> getAuctionBids(Long auctionId) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 사용자의 입찰 내역 조회
     */
    @Transactional(readOnly = true)
    public List<Object> getUserBids(Long bidderId) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 경매의 최고가 입찰 조회
     */
    @Transactional(readOnly = true)
    public Object getHighestBid(Long auctionId) {
        // TODO: 구현 필요
        return null;
    }

}
