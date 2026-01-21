package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
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
        // 1. 입찰 조회
        Bid bid = bidSupport.findByIdOrThrow(bidId);
        
        // 2. 취소 가능 여부 검증
        validateCancellable(bid, bidderId);
        
        // 3. 입찰 상태 변경 (ACTIVE -> CANCELLED)
        bid.cancel();
        
        // 4. 저장
        bidSupport.save(bid);
    }

    /**
     * 취소 가능 여부 검증
     */
    private void validateCancellable(Bid bid, Long bidderId) {
        // 1. 본인의 입찰인지 확인
        if (!bid.getBidderId().equals(bidderId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 2. ACTIVE 상태인지 확인
        if (bid.getStatus() != BidStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.BID_CANCELLED_NOT_ALLOWED);
        }
        
        // 3. 최고가 입찰자가 아닌지 확인
        if (bid.getIsWinning()) {
            throw new BusinessException(ErrorCode.BID_CANCELLED_NOT_ALLOWED);
        }
        
        // 4. 입찰 후 5분 이내인지 확인 (Bid 엔티티의 canCancel() 메서드 활용)
        if (!bid.canCancel()) {
            throw new BusinessException(ErrorCode.BID_CANCELLED_NOT_ALLOWED);
        }
    }

}
