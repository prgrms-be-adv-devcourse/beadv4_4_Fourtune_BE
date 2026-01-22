package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import com.fourtune.auction.boundedContext.auction.domain.entity.CartItem;
import com.fourtune.auction.shared.auction.dto.CartItemResponse;
import com.fourtune.auction.shared.auction.dto.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 장바구니 조회 UseCase
 * - 장바구니 내역 조회
 * - 활성 아이템만 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartQueryUseCase {

    private final CartSupport cartSupport;
    private final AuctionSupport auctionSupport;

    /**
     * 사용자의 장바구니 조회
     */
    public CartResponse getUserCart(Long userId) {
        // 1. 장바구니 조회 (items 포함, LazyInitializationException 방지)
        Optional<Cart> cartOpt = cartSupport.findByUserIdWithItems(userId);
        if (cartOpt.isEmpty()) {
            return createEmptyCartResponse(userId);
        }
        Cart cart = cartOpt.get();
        
        // 2. 모든 아이템 목록 조회 (상태 무관)
        List<CartItem> items = cart.getItems();
        
        // 3. 각 아이템의 경매 정보 조회 후 DTO 변환
        List<CartItemResponse> itemResponses = new ArrayList<>();
        for (CartItem item : items) {
            AuctionItem auctionItem = auctionSupport.findById(item.getAuctionId()).orElse(null);
            itemResponses.add(CartItemResponse.from(item, auctionItem));
        }
        
        // 4. CartResponse 생성 후 반환
        return CartResponse.from(cart, itemResponses);
    }

    /**
     * 장바구니 활성 아이템 개수 조회
     */
    public int getActiveItemCount(Long userId) {
        // 1. 장바구니 조회 (items 포함, LazyInitializationException 방지)
        Optional<Cart> cartOpt = cartSupport.findByUserIdWithItems(userId);
        if (cartOpt.isEmpty()) {
            return 0;
        }
        
        // 2. 활성 아이템 개수 반환
        return cartOpt.get().getActiveItemCount();
    }

    /**
     * 장바구니 총액 계산
     */
    public BigDecimal calculateTotalPrice(Long userId) {
        // 1. 장바구니 조회 (items 포함, LazyInitializationException 방지)
        Optional<Cart> cartOpt = cartSupport.findByUserIdWithItems(userId);
        if (cartOpt.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Cart cart = cartOpt.get();
        
        // 2. 활성 아이템들의 현재 즉시구매가 합계 계산
        return cart.getActiveItems().stream()
                .map(item -> {
                    // 현재 경매의 즉시구매가 조회
                    AuctionItem auctionItem = auctionSupport.findById(item.getAuctionId()).orElse(null);
                    if (auctionItem != null && auctionItem.getBuyNowPrice() != null) {
                        return auctionItem.getBuyNowPrice();
                    }
                    return item.getBuyNowPriceWhenAdded();
                })
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 빈 장바구니 응답 생성
     */
    private CartResponse createEmptyCartResponse(Long userId) {
        return new CartResponse(
                null,
                userId,
                0,
                0,
                BigDecimal.ZERO,
                List.of()
        );
    }

}
