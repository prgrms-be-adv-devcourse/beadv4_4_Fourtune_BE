package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 입찰 취소 UseCase
 * - 입찰 취소 (특정 조건에서만 가능)
 * - 최고가 입찰자는 취소 불가
 */
@Service
@RequiredArgsConstructor
public class BidCancelUseCase {

    private final BidSupport bidSupport;
    private final AuctionSupport auctionSupport;

    /**
     * 입찰 취소
     */
    @Transactional
    public void cancelBid(Long bidId, Long bidderId) {
        // TODO: 구현 필요
        // 1. 입찰 조회
        // 2. 본인의 입찰인지 확인
        // 3. 취소 가능 여부 확인
        //    - 최고가 입찰자가 아닌 경우에만 가능
        //    - 이미 FAILED 상태가 아닌지
        // 4. 입찰 상태 변경 (ACTIVE -> CANCELLED)
    }

    /**
     * 취소 가능 여부 검증
     */
    private void validateCancellable(Bid bid, Long bidderId) {
        // TODO: 구현 필요
        // - 본인의 입찰인지
        // - 최고가 입찰자가 아닌지
        // - ACTIVE 상태인지
    }

}
