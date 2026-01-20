package com.fourtune.auction.boundedContext.watchlist.domain.entity;

import com.fourtune.auction.global.common.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 관심상품 엔티티
 * - 사용자가 관심있는 경매 상품 관리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "watchlists", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "auctionId"}),
       indexes = {
           @Index(name = "idx_watchlist_user_id", columnList = "userId"),
           @Index(name = "idx_watchlist_auction_id", columnList = "auctionId"),
           @Index(name = "idx_watchlist_created_at", columnList = "createdAt")
       })
public class Watchlist extends BaseIdAndTime {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long auctionId;

    // 등록 시점의 경매 정보 (스냅샷)
    @Column(nullable = false, length = 200)
    private String auctionTitle;

    @Column(precision = 12, scale = 2)
    private BigDecimal priceAtAdded;  // 등록 시점 가격

    private LocalDateTime auctionEndTime;  // 경매 종료 시간

    // 알림 설정
    @Column(nullable = false)
    private boolean notifyOnEndingSoon = true;  // 마감 임박 알림

    @Column(nullable = false)
    private boolean notifyOnPriceChange = true;  // 가격 변동 알림

    @Builder
    private Watchlist(Long userId, Long auctionId, String auctionTitle,
                      BigDecimal priceAtAdded, LocalDateTime auctionEndTime,
                      boolean notifyOnEndingSoon, boolean notifyOnPriceChange) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.auctionTitle = auctionTitle;
        this.priceAtAdded = priceAtAdded;
        this.auctionEndTime = auctionEndTime;
        this.notifyOnEndingSoon = notifyOnEndingSoon;
        this.notifyOnPriceChange = notifyOnPriceChange;
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 관심상품 생성
     */
    public static Watchlist create(Long userId, Long auctionId, String auctionTitle,
                                   BigDecimal currentPrice, LocalDateTime auctionEndTime) {
        return Watchlist.builder()
                .userId(userId)
                .auctionId(auctionId)
                .auctionTitle(auctionTitle)
                .priceAtAdded(currentPrice)
                .auctionEndTime(auctionEndTime)
                .notifyOnEndingSoon(true)
                .notifyOnPriceChange(true)
                .build();
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 마감 임박 알림 설정 변경
     */
    public void updateNotifyOnEndingSoon(boolean notify) {
        this.notifyOnEndingSoon = notify;
    }

    /**
     * 가격 변동 알림 설정 변경
     */
    public void updateNotifyOnPriceChange(boolean notify) {
        this.notifyOnPriceChange = notify;
    }

    /**
     * 본인 관심상품 확인
     */
    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }

    /**
     * 경매 종료 임박 여부 확인
     */
    public boolean isEndingSoon(int minutesThreshold) {
        if (this.auctionEndTime == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(minutesThreshold);
        return this.auctionEndTime.isBefore(threshold) && this.auctionEndTime.isAfter(LocalDateTime.now());
    }

}
