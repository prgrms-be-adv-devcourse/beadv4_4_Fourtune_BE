package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;
import com.fourtune.auction.global.common.BaseTimeEntity;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "auction_bids", indexes = {
    @Index(name = "idx_auction_bids_auction_id", columnList = "auction_id"),
    @Index(name = "idx_auction_bids_bidder_id", columnList = "bidder_id"),
    @Index(name = "idx_auction_bids_auction_amount", columnList = "auction_id, bid_amount DESC")
})
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long auctionId;
    
    @Column(nullable = false)
    private Long bidderId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal bidAmount;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BidStatus status = BidStatus.ACTIVE;
    
    // 자동 입찰 여부
    @Builder.Default
    @Column(nullable = false)
    private Boolean isAutoBid = false;
    
    // 최고 입찰 여부 (낙찰자 결정용)
    @Builder.Default
    @Column(nullable = false)
    private Boolean isWinning = false;
    
    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 입찰 생성 정적 팩토리 메서드
     * 비즈니스 규칙 검증 후 엔티티 생성
     */
    public static Bid create(
            Long auctionId,
            Long bidderId,
            BigDecimal bidAmount,
            BigDecimal currentPrice,
            Integer bidUnit,
            Boolean isAutoBid
    ) {
        // 1. 필수 필드 검증
        validateRequired(auctionId, bidderId, bidAmount);
        
        // 2. 입찰가 검증
        validateBidAmount(bidAmount, currentPrice, bidUnit);
        
        // 3. 엔티티 생성
        return Bid.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .bidAmount(bidAmount)
                .status(BidStatus.ACTIVE)
                .isAutoBid(isAutoBid != null ? isAutoBid : false)
                .isWinning(false)
                .build();
    }
    
    /**
     * 입찰 취소 (ACTIVE → CANCELLED)
     * 입찰 후 5분 이내에만 가능
     */
    public void cancel() {
        if (this.status != BidStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.BID_CANCELLED_NOT_ALLOWED);
        }
        
        // 입찰 후 5분 이내 확인 (BidPolicy 활용)
        if (!canCancel()) {
            throw new BusinessException(ErrorCode.BID_CANCELLED_NOT_ALLOWED);
        }
        
        this.status = BidStatus.CANCELLED;
        this.isWinning = false;
    }
    
    /**
     * 낙찰 처리 (ACTIVE → SUCCESS)
     */
    public void win() {
        if (this.status != BidStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.BID_CANCELLED_NOT_ALLOWED);
        }
        
        this.status = BidStatus.SUCCESS;
        this.isWinning = true;
    }
    
    /**
     * 패찰 처리 (ACTIVE → FAILED)
     */
    public void lose() {
        if (this.status != BidStatus.ACTIVE) {
            return; // 이미 처리된 상태면 무시
        }
        
        this.status = BidStatus.FAILED;
        this.isWinning = false;
    }
    
    /**
     * 최고 입찰로 갱신
     */
    public void updateAsHighestBid() {
        if (this.status == BidStatus.ACTIVE) {
            this.isWinning = true;
        }
    }
    
    /**
     * 최고 입찰 해제 (다른 입찰이 더 높을 때)
     */
    public void removeAsHighestBid() {
        if (this.status == BidStatus.ACTIVE) {
            this.isWinning = false;
        }
    }
    
    /**
     * 입찰 취소 가능 여부 확인
     * 입찰 후 5분 이내에만 가능
     */
    public boolean canCancel() {
        if (this.status != BidStatus.ACTIVE) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(this.getCreatedAt(), now);
        
        // 입찰 후 5분 이내인지 확인 (고정값)
        return duration.toMinutes() <= 5;
    }
    
    /**
     * 활성 상태 여부
     */
    public boolean isActive() {
        return this.status == BidStatus.ACTIVE;
    }
    
    /**
     * 낙찰 상태 여부
     */
    public boolean isSuccess() {
        return this.status == BidStatus.SUCCESS;
    }
    
    /**
     * 취소 상태 여부
     */
    public boolean isCancelled() {
        return this.status == BidStatus.CANCELLED;
    }
    
    // ==================== 검증 메서드 (private) ====================
    
    /**
     * 필수 필드 검증
     */
    private static void validateRequired(
            Long auctionId,
            Long bidderId,
            BigDecimal bidAmount
    ) {
        if (auctionId == null || auctionId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (bidderId == null || bidderId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (bidAmount == null || bidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
    }
    
    /**
     * 입찰가 검증
     * 현재가 + 입찰단위 이상이어야 함
     */
    private static void validateBidAmount(
            BigDecimal bidAmount,
            BigDecimal currentPrice,
            Integer bidUnit
    ) {
        // 현재가가 없으면 시작가부터 시작
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        
        // 입찰 단위 기본값
        int unit = (bidUnit != null && bidUnit > 0) ? bidUnit : 1000;
        
        // 현재가 + 입찰단위 이상이어야 함
        BigDecimal minimumBid = currentPrice.add(BigDecimal.valueOf(unit));
        if (bidAmount.compareTo(minimumBid) < 0) {
            throw new BusinessException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
        
        // 입찰 단위로 나누어떨어지는지 확인
        BigDecimal diff = bidAmount.subtract(currentPrice);
        if (diff.remainder(BigDecimal.valueOf(unit)).compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(ErrorCode.BID_UNIT_INVALID);
        }
    }
}
