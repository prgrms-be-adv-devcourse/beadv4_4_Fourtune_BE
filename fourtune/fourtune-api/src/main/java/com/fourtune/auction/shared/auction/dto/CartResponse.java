package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.CartItemStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * 장바구니 응답 DTO
 */
public record CartResponse(
    Long id,
    Long userId,
    Integer totalItemCount,
    Integer activeItemCount,
    BigDecimal totalPrice, // 활성 아이템들의 즉시구매가 합계
    List<CartItemResponse> items
) {
    /**
     * Cart와 CartItemResponse 목록으로 CartResponse 생성
     */
    public static CartResponse from(Cart cart, List<CartItemResponse> items) {
        int activeCount = (int) items.stream()
                .filter(item -> item.status() == CartItemStatus.ACTIVE)
                .count();
        
        BigDecimal totalPrice = items.stream()
                .filter(item -> item.status() == CartItemStatus.ACTIVE)
                .map(item -> item.currentBuyNowPrice() != null 
                        ? item.currentBuyNowPrice() 
                        : item.buyNowPriceWhenAdded())
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items.size(),
                activeCount,
                totalPrice,
                items
        );
    }
}
