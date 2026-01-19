package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.global.common.BaseTimeEntity;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "auction_items")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionItem extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long sellerId;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal startPrice;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer bidUnit = 1000;
    
    // 즉시구매 관련 필드
    @Column(precision = 19, scale = 2)
    private BigDecimal buyNowPrice;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean buyNowEnabled = false;
    
    @Column(nullable = false)
    private LocalDateTime auctionStartTime;
    
    @Column(nullable = false)
    private LocalDateTime auctionEndTime;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionStatus status = AuctionStatus.SCHEDULED;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal currentPrice;
    
    @Builder.Default
    private Long viewCount = 0L;
    
    @Builder.Default
    private Integer watchlistCount = 0;
    
    @Builder.Default
    private Integer bidCount = 0;
    
    @OneToMany(mappedBy = "auctionItem", cascade = ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemImage> images = new ArrayList<>();
    
    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 경매 생성 정적 팩토리 메서드
     * 비즈니스 규칙 검증 후 엔티티 생성
     */
    public static AuctionItem create(
            Long sellerId,
            String title,
            String description,
            Category category,
            BigDecimal startPrice,
            Integer bidUnit,
            BigDecimal buyNowPrice,
            Boolean buyNowEnabled,
            LocalDateTime auctionStartTime,
            LocalDateTime auctionEndTime
    ) {
        // 1. 필수 필드 검증
        validateRequired(sellerId, title, category, startPrice, auctionStartTime, auctionEndTime);
        
        // 2. 시작가 검증
        validateStartPrice(startPrice);
        
        // 3. 경매 기간 검증
        validateAuctionPeriod(auctionStartTime, auctionEndTime);
        
        // 4. 즉시구매가 검증
        validateBuyNowPrice(buyNowEnabled, buyNowPrice, startPrice);
        
        // 5. 엔티티 생성
        return AuctionItem.builder()
                .sellerId(sellerId)
                .title(title)
                .description(description)
                .category(category)
                .startPrice(startPrice)
                .bidUnit(bidUnit != null ? bidUnit : 1000)
                .buyNowPrice(buyNowPrice)
                .buyNowEnabled(buyNowEnabled != null ? buyNowEnabled : false)
                .auctionStartTime(auctionStartTime)
                .auctionEndTime(auctionEndTime)
                .status(AuctionStatus.SCHEDULED)
                .currentPrice(startPrice)
                .viewCount(0L)
                .watchlistCount(0)
                .bidCount(0)
                .images(new ArrayList<>())
                .build();
    }
    
    /**
     * 경매 정보 수정
     * SCHEDULED 또는 ACTIVE 상태에서만 수정 가능
     */
    public void update(
            String title,
            String description,
            BigDecimal buyNowPrice,
            Boolean buyNowEnabled
    ) {
        // 수정 가능 상태 검증
        if (this.status != AuctionStatus.SCHEDULED && this.status != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_MODIFIABLE);
        }
        
        // 필드 업데이트
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        
        // 즉시구매가 업데이트 (검증 포함)
        if (buyNowEnabled != null && buyNowEnabled) {
            if (buyNowPrice == null) {
                throw new BusinessException(ErrorCode.BUY_NOW_PRICE_NOT_SET);
            }
            if (buyNowPrice.compareTo(this.startPrice) <= 0) {
                throw new BusinessException(ErrorCode.AUCTION_INVALID_PRICE);
            }
            this.buyNowPrice = buyNowPrice;
            this.buyNowEnabled = true;
        } else if (buyNowEnabled != null && !buyNowEnabled) {
            this.buyNowEnabled = false;
            this.buyNowPrice = null;
        }
    }
    
    /**
     * 경매 시작 (SCHEDULED → ACTIVE)
     */
    public void start() {
        if (this.status != AuctionStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_MODIFIABLE);
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.auctionStartTime)) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_MODIFIABLE);
        }
        
        this.status = AuctionStatus.ACTIVE;
    }
    
    /**
     * 경매 종료 (ACTIVE → ENDED)
     */
    public void close() {
        if (this.status != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_MODIFIABLE);
        }
        
        this.status = AuctionStatus.ENDED;
    }
    
    /**
     * 낙찰 완료 (ENDED → SOLD)
     * 입찰을 통한 낙찰
     */
    public void sell() {
        if (this.status != AuctionStatus.ENDED) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_MODIFIABLE);
        }
        
        this.status = AuctionStatus.SOLD;
    }
    
    /**
     * 경매 취소 (SCHEDULED 또는 ACTIVE → CANCELLED)
     * 입찰이 없는 경우에만 가능
     */
    public void cancel() {
        if (this.status != AuctionStatus.SCHEDULED && this.status != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_MODIFIABLE);
        }
        
        // 입찰이 있으면 취소 불가
        if (this.bidCount > 0) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_MODIFIABLE);
        }
        
        this.status = AuctionStatus.CANCELLED;
    }
    
    /**
     * 경매 자동 연장
     * 종료 시간 N분 연장
     */
    public void extend(int minutes) {
        if (this.status != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_MODIFIABLE);
        }
        
        this.auctionEndTime = this.auctionEndTime.plusMinutes(minutes);
    }
    
    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
    }
    
    /**
     * 입찰 수 증가
     */
    public void increaseBidCount() {
        this.bidCount++;
    }
    
    /**
     * 현재가 업데이트
     * 입찰 시 호출
     */
    public void updateCurrentPrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.AUCTION_INVALID_PRICE);
        }
        
        this.currentPrice = newPrice;
    }
    
    /**
     * 즉시구매 실행 (ACTIVE → SOLD_BY_BUY_NOW)
     * 즉시구매 시 경매 즉시 종료
     */
    public void executeBuyNow() {
        // 즉시구매 가능 여부 검증
        if (!this.buyNowEnabled) {
            throw new BusinessException(ErrorCode.BUY_NOW_NOT_ENABLED);
        }
        if (this.buyNowPrice == null) {
            throw new BusinessException(ErrorCode.BUY_NOW_PRICE_NOT_SET);
        }
        if (this.status != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        
        // 상태 변경
        this.status = AuctionStatus.SOLD_BY_BUY_NOW;
        this.currentPrice = this.buyNowPrice;
    }
    
    /**
     * 장바구니 담기 가능 여부 확인
     * 조건: 즉시구매 활성화 + ACTIVE 상태
     */
    public boolean canAddToCart() {
        return this.status == AuctionStatus.ACTIVE 
                && this.buyNowEnabled 
                && this.buyNowPrice != null;
    }
    
    /**
     * 입찰 가능 여부 확인
     */
    public boolean canBid() {
        return this.status == AuctionStatus.ACTIVE;
    }
    
    // ==================== 검증 메서드 (private) ====================
    
    /**
     * 필수 필드 검증
     */
    private static void validateRequired(
            Long sellerId, 
            String title, 
            Category category, 
            BigDecimal startPrice,
            LocalDateTime auctionStartTime, 
            LocalDateTime auctionEndTime
    ) {
        if (sellerId == null || sellerId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (startPrice == null) {
            throw new BusinessException(ErrorCode.AUCTION_INVALID_PRICE);
        }
        if (auctionStartTime == null || auctionEndTime == null) {
            throw new BusinessException(ErrorCode.AUCTION_INVALID_DURATION);
        }
    }
    
    /**
     * 시작가 검증
     * 최소 1,000원 이상
     */
    private static void validateStartPrice(BigDecimal startPrice) {
        if (startPrice.compareTo(AuctionPolicy.MIN_START_PRICE) < 0) {
            throw new BusinessException(ErrorCode.AUCTION_INVALID_PRICE);
        }
    }
    
    /**
     * 경매 기간 검증
     * 최소 1시간 ~ 최대 30일
     */
    private static void validateAuctionPeriod(LocalDateTime startTime, LocalDateTime endTime) {
        // 종료 시간이 시작 시간보다 이후여야 함
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new BusinessException(ErrorCode.AUCTION_INVALID_DURATION);
        }
        
        // 경매 기간 계산
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        long days = duration.toDays();
        
        // 최소 1시간
        if (hours < AuctionPolicy.MIN_AUCTION_DURATION_HOURS) {
            throw new BusinessException(ErrorCode.AUCTION_INVALID_DURATION);
        }
        
        // 최대 30일
        if (days > AuctionPolicy.MAX_AUCTION_DURATION_DAYS) {
            throw new BusinessException(ErrorCode.AUCTION_INVALID_DURATION);
        }
    }
    
    /**
     * 즉시구매가 검증
     * 즉시구매 활성화 시: buyNowPrice 필수, 시작가보다 높아야 함
     */
    private static void validateBuyNowPrice(Boolean buyNowEnabled, BigDecimal buyNowPrice, BigDecimal startPrice) {
        if (buyNowEnabled != null && buyNowEnabled) {
            // 즉시구매가 활성화되어 있는데 가격이 없으면 예외
            if (buyNowPrice == null) {
                throw new BusinessException(ErrorCode.BUY_NOW_PRICE_NOT_SET);
            }
            
            // 즉시구매가는 시작가보다 높아야 함
            if (buyNowPrice.compareTo(startPrice) <= 0) {
                throw new BusinessException(ErrorCode.AUCTION_INVALID_PRICE);
            }
        }
    }
}
