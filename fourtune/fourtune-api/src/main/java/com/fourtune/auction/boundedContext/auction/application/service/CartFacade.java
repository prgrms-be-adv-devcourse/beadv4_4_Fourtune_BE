package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.common.shared.auction.dto.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 장바구니 Facade
 * - 여러 UseCase를 조합하여 복잡한 비즈니스 플로우 처리
 * - Controller는 이 Facade만 호출
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartFacade {

    private final CartAddItemUseCase cartAddItemUseCase;
    private final CartRemoveItemUseCase cartRemoveItemUseCase;
    private final CartQueryUseCase cartQueryUseCase;
    private final CartBuyNowUseCase cartBuyNowUseCase;

    /**
     * 장바구니에 아이템 추가
     */
    public void addItemToCart(Long userId, Long auctionId) {
        // CartAddItemUseCase 호출
        cartAddItemUseCase.addItemToCart(userId, auctionId);
    }

    /**
     * 장바구니에서 아이템 제거
     */
    public void removeItemFromCart(Long userId, Long cartItemId) {
        // CartRemoveItemUseCase 호출
        cartRemoveItemUseCase.removeItemFromCart(userId, cartItemId);
    }

    /**
     * 사용자의 장바구니 조회
     */
    @Transactional(readOnly = true)
    public CartResponse getUserCart(Long userId) {
        // CartQueryUseCase.getUserCart 호출
        return cartQueryUseCase.getUserCart(userId);
    }

    /**
     * 장바구니에서 선택 아이템 즉시구매
     */
    public List<String> buyNowFromCart(Long userId, List<Long> cartItemIds) {
        // CartBuyNowUseCase.buyNowFromCart 호출
        return cartBuyNowUseCase.buyNowFromCart(userId, cartItemIds);
    }

    /**
     * 장바구니 전체 즉시구매
     */
    public List<String> buyNowAllCart(Long userId) {
        // CartBuyNowUseCase.buyNowAllCart 호출
        return cartBuyNowUseCase.buyNowAllCart(userId);
    }

    /**
     * 만료된 아이템 일괄 제거
     */
    public void clearExpiredItems(Long userId) {
        // CartRemoveItemUseCase.removeExpiredItems 호출
        cartRemoveItemUseCase.removeExpiredItems(userId);
    }

    /**
     * 구매완료 아이템 일괄 제거
     */
    public void clearPurchasedItems(Long userId) {
        // CartRemoveItemUseCase.removePurchasedItems 호출
        cartRemoveItemUseCase.removePurchasedItems(userId);
    }

    /**
     * 장바구니 활성 아이템 개수 조회
     */
    @Transactional(readOnly = true)
    public int getActiveItemCount(Long userId) {
        return cartQueryUseCase.getActiveItemCount(userId);
    }

}
