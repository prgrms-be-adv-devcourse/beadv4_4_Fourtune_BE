package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.CartItemStatus;
import com.fourtune.common.global.common.BaseTimeEntity;
import com.fourtune.common.global.error.ErrorCode;
import com.fourtune.common.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "auction_carts")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long userId;
    
    @OneToMany(mappedBy = "cart", cascade = ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    
    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 경매 상품 추가
     * 중복 검증 포함
     */
    public void addItem(Long auctionId, BigDecimal buyNowPrice) {
        // 중복 체크
        if (hasItem(auctionId)) {
            throw new BusinessException(ErrorCode.CART_ITEM_ALREADY_EXISTS);
        }
        
        CartItem item = CartItem.builder()
            .cart(this)
            .auctionId(auctionId)
            .buyNowPriceWhenAdded(buyNowPrice)
            .status(CartItemStatus.ACTIVE)
            .build();
        
        items.add(item);
    }
    
    /**
     * 장바구니 아이템 제거
     */
    public void removeItem(Long cartItemId) {
        items.removeIf(item -> item.getId().equals(cartItemId));
    }
    
    /**
     * 구매 완료 아이템 정리
     */
    public void clearPurchasedItems() {
        items.removeIf(item -> item.getStatus() == CartItemStatus.PURCHASED);
    }
    
    /**
     * 만료 아이템 정리
     */
    public void clearExpiredItems() {
        items.removeIf(item -> item.getStatus() == CartItemStatus.EXPIRED);
    }
    
    /**
     * 활성 아이템 목록 조회
     */
    public List<CartItem> getActiveItems() {
        return items.stream()
            .filter(item -> item.getStatus() == CartItemStatus.ACTIVE)
            .collect(Collectors.toList());
    }
    
    /**
     * 활성 아이템 개수
     */
    public int getActiveItemCount() {
        return (int) items.stream()
            .filter(item -> item.getStatus() == CartItemStatus.ACTIVE)
            .count();
    }
    
    // ==================== 검증 메서드 (private) ====================
    
    /**
     * 중복 확인
     * 같은 경매 상품이 활성 상태로 담겨있는지 확인
     */
    private boolean hasItem(Long auctionId) {
        return items.stream()
            .anyMatch(item -> item.getAuctionId().equals(auctionId) 
                && item.getStatus() == CartItemStatus.ACTIVE);
    }
}
