package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.event.BidPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

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
    private final EventPublisher eventPublisher;
    // private final RedisLockService redisLockService; // TODO: 나중에 추가

    /**
     * 입찰 등록
     */
    @Transactional
    public Long placeBid(Long auctionId, Long bidderId, BigDecimal bidAmount) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 입찰 가능 여부 확인
        validateBidPlaceable(auctionItem, bidderId, bidAmount);
        
        // 3. 이전 최고가 입찰자 정보 조회 및 처리
        Optional<Bid> previousHighestBid = bidSupport.findHighestBid(auctionId);
        if (previousHighestBid.isPresent()) {
            Bid prevBid = previousHighestBid.get();
            // 이전 최고 입찰자 상태 해제
            prevBid.removeAsHighestBid();
            bidSupport.save(prevBid);
        }
        
        // 4. 새 입찰 생성 및 저장
        Bid newBid = Bid.create(
                auctionId,
                bidderId,
                bidAmount,
                auctionItem.getCurrentPrice(),
                auctionItem.getBidUnit(),
                false  // isAutoBid
        );
        newBid.updateAsHighestBid();
        Bid savedBid = bidSupport.save(newBid);
        
        // 5. 경매의 currentPrice 업데이트 및 bidCount 증가
        auctionItem.updateCurrentPrice(bidAmount);
        auctionItem.increaseBidCount();
        auctionSupport.save(auctionItem);
        
        // 6. 자동 연장 체크 (종료 5분 전이면 연장)
        checkAndExtendAuction(auctionItem);
        
        // 7. 이벤트 발행 (실시간 알림 등에 사용)
        eventPublisher.publish(new BidPlacedEvent(
                savedBid.getId(),
                auctionId,
                bidderId,
                bidAmount,
                LocalDateTime.now()
        ));
        
        // 8. 입찰 ID 반환
        return savedBid.getId();
    }

    /**
     * 입찰 가능 여부 검증
     */
    private void validateBidPlaceable(AuctionItem auctionItem, Long bidderId, BigDecimal bidAmount) {
        // 1. 경매가 ACTIVE 상태인지
        if (auctionItem.getStatus() != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.BID_NOT_ALLOWED);
        }
        
        // 2. 경매 종료 시간이 지나지 않았는지
        if (LocalDateTime.now().isAfter(auctionItem.getAuctionEndTime())) {
            throw new BusinessException(ErrorCode.AUCTION_ALREADY_ENDED);
        }
        
        // 3. 본인이 판매자가 아닌지
        if (auctionItem.getSellerId().equals(bidderId)) {
            throw new BusinessException(ErrorCode.BID_SELF_AUCTION);
        }
        
        // 4. 이미 최고 입찰자인지 확인
        Optional<Bid> highestBid = bidSupport.findHighestBid(auctionItem.getId());
        if (highestBid.isPresent() && highestBid.get().getBidderId().equals(bidderId)) {
            throw new BusinessException(ErrorCode.BID_ALREADY_HIGHEST);
        }
        
        // 5. 입찰 금액이 현재가 + 입찰단위 이상인지
        BigDecimal currentPrice = auctionItem.getCurrentPrice() != null 
                ? auctionItem.getCurrentPrice() 
                : auctionItem.getStartPrice();
        BigDecimal minimumBid = currentPrice.add(BigDecimal.valueOf(auctionItem.getBidUnit()));
        
        if (bidAmount.compareTo(minimumBid) < 0) {
            throw new BusinessException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
    }

    /**
     * 자동 연장 체크 및 처리
     * 종료 시간 5분 전이면 3분 연장
     */
    private void checkAndExtendAuction(AuctionItem auctionItem) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auctionItem.getAuctionEndTime();
        Duration remaining = Duration.between(now, endTime);
        
        // 종료 5분 전이면 자동 연장
        if (remaining.toMinutes() <= AuctionPolicy.AUTO_EXTEND_THRESHOLD_MINUTES) {
            auctionExtendUseCase.extendAuction(auctionItem.getId());
        }
    }

}
