package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.CartItemStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import com.fourtune.auction.boundedContext.auction.domain.entity.CartItem;
import com.fourtune.auction.boundedContext.auction.port.out.CartRepository;
import com.fourtune.auction.boundedContext.auction.port.out.CartItemRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 장바구니 공통 조회/검증 기능
 * - Repository 직접 호출
 * - 여러 UseCase에서 재사용되는 공통 로직
 */
@Component
@RequiredArgsConstructor
public class CartSupport {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * 사용자 ID로 장바구니 조회 (Optional)
     */
    public Optional<Cart> findByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    /**
     * 사용자 ID로 장바구니 조회 (없으면 생성)
     */
    public Cart findOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }

    /**
     * 장바구니 생성
     */
    private Cart createCart(Long userId) {
        Cart cart = Cart.builder()
                .userId(userId)
                .build();
        return cartRepository.save(cart);
    }

    /**
     * 장바구니 저장
     */
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    /**
     * 장바구니 아이템 ID로 조회 (예외 발생)
     */
    public CartItem findCartItemByIdOrThrow(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    /**
     * 장바구니 ID로 아이템 목록 조회
     */
    public List<CartItem> findCartItemsByCartId(Long cartId) {
        return cartItemRepository.findByCartIdAndStatus(cartId, CartItemStatus.ACTIVE);
    }

    /**
     * 경매 ID로 활성 상태의 장바구니 아이템들 만료 처리
     * 경매 종료 시 해당 경매의 장바구니 아이템 만료
     */
    public void expireCartItemsByAuctionId(Long auctionId) {
        List<CartItem> activeItems = cartItemRepository
                .findByAuctionIdAndStatus(auctionId, CartItemStatus.ACTIVE);
        activeItems.forEach(CartItem::markAsExpired);
        cartItemRepository.saveAll(activeItems);
    }

    /**
     * 장바구니의 구매완료 아이템 삭제
     */
    public void deletePurchasedItems(Long cartId) {
        List<CartItem> purchasedItems = cartItemRepository
                .findByCartIdAndStatus(cartId, CartItemStatus.PURCHASED);
        cartItemRepository.deleteAll(purchasedItems);
    }

}
