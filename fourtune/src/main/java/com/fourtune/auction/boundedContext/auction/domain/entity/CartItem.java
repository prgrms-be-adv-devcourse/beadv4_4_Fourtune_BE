package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.CartItemStatus;
import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "auction_cart_items", indexes = {
    @Index(name = "idx_auction_cart_items_cart_id", columnList = "cart_id"),
    @Index(name = "idx_auction_cart_items_auction_id", columnList = "auction_id")
})
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @Column(nullable = false)
    private Long auctionId;
    
    // 담았을 때의 즉시구매가 (가격 변동 추적용)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal buyNowPriceWhenAdded;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CartItemStatus status = CartItemStatus.ACTIVE;
    
    // 비즈니스 메서드
    
    /**
     * 구매 완료 처리
     */
    public void markAsPurchased() {
        this.status = CartItemStatus.PURCHASED;
    }
    
    /**
     * 만료 처리 (경매 종료 시)
     */
    public void markAsExpired() {
        this.status = CartItemStatus.EXPIRED;
    }
    
    /**
     * 활성 상태 여부
     */
    public boolean isActive() {
        return this.status == CartItemStatus.ACTIVE;
    }
    
    /**
     * 구매 완료 여부
     */
    public boolean isPurchased() {
        return this.status == CartItemStatus.PURCHASED;
    }
    
    /**
     * 만료 여부
     */
    public boolean isExpired() {
        return this.status == CartItemStatus.EXPIRED;
    }
}
