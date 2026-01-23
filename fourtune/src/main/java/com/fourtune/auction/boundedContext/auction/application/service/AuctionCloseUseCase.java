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
     * 동시성 제어: Pessimistic Lock 적용
     */
    @Transactional
    public void closeAuction(Long auctionId) {
        // 1. 경매 조회 (Pessimistic Lock 적용)
        AuctionItem auctionItem = auctionSupport.findByIdWithLockOrThrow(auctionId);
        
        // 2. 종료 가능 여부 확인
        validateCloseable(auctionItem);
        
        // 3. 최고가 입찰 조회
        java.util.Optional<Bid> highestBidOpt = bidSupport.findHighestBid(auctionId);
        
        if (highestBidOpt.isPresent()) {
            // 4-1. 낙찰자가 있는 경우
            Bid winningBid = highestBidOpt.get();
            
            // 경매 상태 변경 (ACTIVE -> SOLD)
            auctionItem.sell();
            
            // 낙찰 입찰 처리
            winningBid.win();
            bidSupport.save(winningBid);
            
            // Order 생성 (엔티티 직접 전달하여 중복 Lock 방지)
            String orderId = orderCreateUseCase.createWinningOrder(
                    auctionItem,
                    winningBid.getBidderId(),
                    winningBid.getBidAmount()
            );
            
            // 실패한 입찰들 처리
            bidSupport.failAllActiveBids(auctionId);
            
            // 이벤트 발행
            eventPublisher.publish(new AuctionClosedEvent(
                    auctionId,
                    auctionItem.getTitle(),
                    auctionItem.getSellerId(),
                    winningBid.getBidderId(),
                    winningBid.getBidAmount(),
                    orderId
            ));
        } else {
            // 4-2. 낙찰자가 없는 경우 (입찰이 없었음)
            auctionItem.close();
            
            // 이벤트 발행
            eventPublisher.publish(new AuctionClosedEvent(
                    auctionId,
                    auctionItem.getTitle(),
                    auctionItem.getSellerId(),
                    null,  // 낙찰자 없음
                    null,  // 낙찰가 없음
                    null   // 주문번호 없음
            ));
        }
    }

    /**
     * 종료 가능 여부 검증
     */
    private void validateCloseable(AuctionItem auctionItem) {
        // ACTIVE 상태인지 확인
        if (auctionItem.getStatus() != com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.ACTIVE) {
            throw new com.fourtune.auction.global.error.exception.BusinessException(
                    com.fourtune.auction.global.error.ErrorCode.AUCTION_NOT_ACTIVE
            );
        }
        
        // 종료 시간이 지났는지 확인
        if (java.time.LocalDateTime.now().isBefore(auctionItem.getAuctionEndTime())) {
            throw new com.fourtune.auction.global.error.exception.BusinessException(
                    com.fourtune.auction.global.error.ErrorCode.AUCTION_NOT_MODIFIABLE
            );
        }
    }

}
