package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import com.fourtune.auction.boundedContext.auction.domain.entity.CartItem;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 장바구니 아이템 제거 UseCase
 * - 개별 아이템 제거
 * - 만료된 아이템 일괄 제거
 */
@Service
@RequiredArgsConstructor
public class CartRemoveItemUseCase {

    private final CartSupport cartSupport;

    /**
     * 장바구니에서 아이템 제거
     */
    @Transactional
    public void removeItemFromCart(Long userId, Long cartItemId) {
        // 1. 장바구니 아이템 조회 (Cart 포함, LazyInitializationException 방지)
        CartItem cartItem = cartSupport.findCartItemByIdWithCartOrThrow(cartItemId);
        
        // 2. 장바구니 조회
        Optional<Cart> cartOpt = cartSupport.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_NOT_FOUND);
        }
        Cart cart = cartOpt.get();
        
        // 3. 본인의 장바구니인지 확인
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 4. 아이템 제거 (Cart.removeItem 메서드 사용)
        cart.removeItem(cartItemId);
        
        // 5. DB 저장 (orphanRemoval = true로 자동 삭제)
        cartSupport.save(cart);
    }

    /**
     * 만료된 아이템 제거
     */
    @Transactional
    public void removeExpiredItems(Long userId) {
        // 1. 장바구니 조회
        Optional<Cart> cartOpt = cartSupport.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return; // 장바구니가 없으면 처리할 것 없음
        }
        Cart cart = cartOpt.get();
        
        // 2. 만료된 아이템 제거 (Cart.clearExpiredItems 메서드 사용)
        cart.clearExpiredItems();
        
        // 3. DB 저장
        cartSupport.save(cart);
    }

    /**
     * 구매완료 아이템 제거
     */
    @Transactional
    public void removePurchasedItems(Long userId) {
        // 1. 장바구니 조회
        Optional<Cart> cartOpt = cartSupport.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return;
        }
        Cart cart = cartOpt.get();
        
        // 2. 구매완료 아이템 제거 (Cart.clearPurchasedItems 메서드 사용)
        cart.clearPurchasedItems();
        
        // 3. DB 저장
        cartSupport.save(cart);
    }

}
