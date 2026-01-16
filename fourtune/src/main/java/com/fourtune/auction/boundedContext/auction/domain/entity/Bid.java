package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;
import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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
    
    // 비즈니스 메서드
    
    public void cancel() {
        if (this.status != BidStatus.ACTIVE) {
            throw new IllegalStateException("취소할 수 없는 입찰 상태입니다.");
        }
        this.status = BidStatus.CANCELLED;
        this.isWinning = false;
    }
    
    public void markAsWinning() {
        if (this.status != BidStatus.ACTIVE) {
            throw new IllegalStateException("낙찰 처리할 수 없는 입찰 상태입니다.");
        }
        this.status = BidStatus.SUCCESS;
        this.isWinning = true;
    }
    
    public void markAsFailed() {
        if (this.status != BidStatus.ACTIVE) {
            return; // 이미 처리된 상태면 무시
        }
        this.status = BidStatus.FAILED;
        this.isWinning = false;
    }
    
    public void setWinning(boolean isWinning) {
        this.isWinning = isWinning;
    }
}
