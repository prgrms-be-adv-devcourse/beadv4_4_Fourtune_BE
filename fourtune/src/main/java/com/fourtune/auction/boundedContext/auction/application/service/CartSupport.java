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
        // TODO: 구현 필요
        return Optional.empty();
    }

    /**
     * 사용자 ID로 장바구니 조회 (없으면 생성)
     */
    public Cart findOrCreateCart(Long userId) {
        // TODO: 구현 필요
        // return cartRepository.findByUserId(userId)
        //         .orElseGet(() -> createCart(userId));
        return null;
    }

    /**
     * 장바구니 생성
     */
    private Cart createCart(Long userId) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 장바구니 저장
     */
    public Cart save(Cart cart) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 장바구니 아이템 ID로 조회 (예외 발생)
     */
    public CartItem findCartItemByIdOrThrow(Long cartItemId) {
        // TODO: 구현 필요
        // return cartItemRepository.findById(cartItemId)
        //         .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        return null;
    }

    /**
     * 장바구니 ID로 아이템 목록 조회
     */
    public List<CartItem> findCartItemsByCartId(Long cartId) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 경매 ID로 활성 상태의 장바구니 아이템들 만료 처리
     */
    public void expireCartItemsByAuctionId(Long auctionId) {
        // TODO: 구현 필요
        // @Modifying @Query 사용
    }

    /**
     * 장바구니의 구매완료 아이템 삭제
     */
    public void deletePurchasedItems(Long cartId) {
        // TODO: 구현 필요
        // @Modifying @Query 사용
    }

}
