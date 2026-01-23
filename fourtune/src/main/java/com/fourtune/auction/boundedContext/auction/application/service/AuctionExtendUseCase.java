package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.constant.BidPolicy;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.event.AuctionExtendedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 경매 자동 연장 UseCase
 * - 종료 5분 전 입찰 시 자동 연장
 * - 연장 횟수 제한
 */
@Service
@RequiredArgsConstructor
public class AuctionExtendUseCase {

    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;
    private final BidPolicy bidPolicy;

    /**
     * 경매 자동 연장
     * 동시성 제어: Pessimistic Lock 적용
     */
    @Transactional
    public void extendAuction(Long auctionId) {
        // 1. 경매 조회 (Pessimistic Lock 적용)
        AuctionItem auctionItem = auctionSupport.findByIdWithLockOrThrow(auctionId);
        
        // 2. 연장 가능 여부 확인
        validateExtendable(auctionItem);
        
        // 3. 경매 시간 연장 (3분 연장)
        auctionItem.extend(AuctionPolicy.AUTO_EXTEND_MINUTES);
        
        // 4. DB 저장 (dirty checking)
        
        // 5. 이벤트 발행
        eventPublisher.publish(new AuctionExtendedEvent(
                auctionId,
                auctionItem.getAuctionEndTime()  // 새로운 종료 시간
        ));
    }

    /**
     * 자동 연장 필요 여부 확인
     */
    public boolean needsExtension(Long auctionId, LocalDateTime bidTime) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 종료 시간과 입찰 시간 차이 계산
        java.time.Duration remaining = java.time.Duration.between(
                bidTime, 
                auctionItem.getAuctionEndTime()
        );
        
        // 3. AuctionPolicy.AUTO_EXTEND_THRESHOLD_MINUTES (5분) 이내면 true
        return remaining.toMinutes() <= AuctionPolicy.AUTO_EXTEND_THRESHOLD_MINUTES;
    }

    /**
     * 연장 가능 여부 검증
     */
    private void validateExtendable(AuctionItem auctionItem) {
        // ACTIVE 상태인지 확인
        if (auctionItem.getStatus() != com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        
        // 연장 횟수 제한 확인
        if (auctionItem.getExtensionCount() >= bidPolicy.getMaxAutoExtendCount()) {
            throw new BusinessException(ErrorCode.AUCTION_MAX_EXTENSION_REACHED);
        }
    }

}
