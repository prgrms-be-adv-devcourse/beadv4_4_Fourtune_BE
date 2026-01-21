package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.shared.auction.dto.BidDetailResponse;
import com.fourtune.auction.shared.auction.dto.BidHistoryResponse;
import com.fourtune.auction.shared.auction.dto.BidResponse;
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
    private final AuctionSupport auctionSupport;

    /**
     * 입찰하기
     */
    public BidDetailResponse placeBid(Long auctionId, Long bidderId, BigDecimal bidAmount) {
        // 1. BidPlaceUseCase 호출 (입찰 등록)
        //    - 내부에서 자동 연장 체크 및 이벤트 발행까지 처리
        Long bidId = bidPlaceUseCase.placeBid(auctionId, bidderId, bidAmount);
        
        // 2. 등록된 입찰 상세 조회 후 반환
        return bidQueryUseCase.getBidDetail(bidId);
    }

    /**
     * 입찰 취소
     */
    public void cancelBid(Long bidId, Long bidderId) {
        // BidCancelUseCase 호출
        bidCancelUseCase.cancelBid(bidId, bidderId);
    }

    /**
     * 경매의 입찰 내역 조회
     */
    @Transactional(readOnly = true)
    public BidHistoryResponse getAuctionBids(Long auctionId) {
        // BidQueryUseCase 호출
        return bidQueryUseCase.getAuctionBids(auctionId);
    }

    /**
     * 사용자의 입찰 내역 조회
     */
    @Transactional(readOnly = true)
    public List<BidResponse> getUserBids(Long bidderId) {
        // BidQueryUseCase 호출
        return bidQueryUseCase.getUserBids(bidderId);
    }

    /**
     * 경매의 최고가 입찰 조회
     */
    @Transactional(readOnly = true)
    public BidResponse getHighestBid(Long auctionId) {
        // BidQueryUseCase 호출
        return bidQueryUseCase.getHighestBid(auctionId);
    }

    /**
     * 입찰 상세 조회
     */
    @Transactional(readOnly = true)
    public BidDetailResponse getBidDetail(Long bidId) {
        // BidQueryUseCase 호출
        return bidQueryUseCase.getBidDetail(bidId);
    }

}
