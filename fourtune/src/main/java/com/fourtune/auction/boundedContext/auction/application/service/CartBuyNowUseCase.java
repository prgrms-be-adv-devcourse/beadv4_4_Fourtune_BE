package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.CartItemStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import com.fourtune.auction.boundedContext.auction.domain.entity.CartItem;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 장바구니 즉시구매 UseCase
 * - 장바구니에서 선택한 상품들 즉시구매
 * - AuctionBuyNowUseCase를 내부적으로 호출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartBuyNowUseCase {

    private final CartSupport cartSupport;
    private final AuctionBuyNowUseCase auctionBuyNowUseCase;

    /**
     * 장바구니에서 선택 아이템 즉시구매
     */
    @Transactional
    public List<String> buyNowFromCart(Long userId, List<Long> cartItemIds) {
        // 1. 장바구니 조회
        Optional<Cart> cartOpt = cartSupport.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_NOT_FOUND);
        }
        Cart cart = cartOpt.get();
        
        List<String> orderIds = new ArrayList<>();
        
        // 2. 각 CartItem에 대해 즉시구매 처리
        for (Long cartItemId : cartItemIds) {
            // 2-1. CartItem 조회
            CartItem cartItem = cartSupport.findCartItemByIdOrThrow(cartItemId);
            
            // 2-2. 본인의 장바구니인지 확인
            if (!cartItem.getCart().getId().equals(cart.getId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            
            // 2-3. CartItem 상태가 ACTIVE인지 확인
            if (cartItem.getStatus() != CartItemStatus.ACTIVE) {
                throw new BusinessException(ErrorCode.CART_ITEM_NOT_ACTIVE);
            }
            
            try {
                // 3. 즉시구매 실행
                String orderId = auctionBuyNowUseCase.executeBuyNow(
                        cartItem.getAuctionId(), 
                        userId
                );
                
                // 4. 즉시구매 성공 시 CartItem 상태 변경 (ACTIVE -> PURCHASED)
                cartItem.markAsPurchased();
                
                // 5. orderId 수집
                orderIds.add(orderId);
                
                log.info("장바구니 즉시구매 성공: cartItemId={}, auctionId={}, orderId={}", 
                        cartItemId, cartItem.getAuctionId(), orderId);
                
            } catch (Exception e) {
                log.error("장바구니 즉시구매 실패: cartItemId={}, auctionId={}, error={}", 
                        cartItemId, cartItem.getAuctionId(), e.getMessage());
                // 하나 실패해도 다른 것은 계속 처리 (또는 전체 롤백 정책에 따라 throw)
                throw e; // 일단 전체 롤백
            }
        }
        
        // 6. 장바구니 저장
        cartSupport.save(cart);
        
        return orderIds;
    }

    /**
     * 장바구니 전체 즉시구매
     */
    @Transactional
    public List<String> buyNowAllCart(Long userId) {
        // 1. 장바구니 조회
        Optional<Cart> cartOpt = cartSupport.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return List.of();
        }
        Cart cart = cartOpt.get();
        
        // 2. 활성 상태의 모든 아이템 ID 수집
        List<Long> activeItemIds = cart.getActiveItems().stream()
                .map(CartItem::getId)
                .toList();
        
        if (activeItemIds.isEmpty()) {
            return List.of();
        }
        
        // 3. buyNowFromCart 호출
        return buyNowFromCart(userId, activeItemIds);
    }

}
