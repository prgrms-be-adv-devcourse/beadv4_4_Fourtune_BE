package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // TODO: 구현 필요
        // 1. 장바구니 조회
        // 2. 본인의 장바구니인지 확인
        // 3. 아이템 제거 (Cart.removeItem 메서드 사용)
        // 4. DB 저장 (orphanRemoval = true로 자동 삭제)
    }

    /**
     * 만료된 아이템 제거
     */
    @Transactional
    public void removeExpiredItems(Long userId) {
        // TODO: 구현 필요
        // 1. 장바구니 조회
        // 2. 만료된 아이템 제거 (Cart.clearExpiredItems 메서드 사용)
        // 3. DB 저장
    }

    /**
     * 구매완료 아이템 제거
     */
    @Transactional
    public void removePurchasedItems(Long userId) {
        // TODO: 구현 필요
        // 1. 장바구니 조회
        // 2. 구매완료 아이템 제거 (Cart.clearPurchasedItems 메서드 사용)
        // 3. DB 저장
    }

}
