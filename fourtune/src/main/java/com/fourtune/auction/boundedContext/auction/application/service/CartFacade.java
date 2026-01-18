package com.fourtune.auction.boundedContext.auction.application.service;

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
        // TODO: 구현 필요
        // 1. CartAddItemUseCase 호출
        // 2. 만료된 아이템 자동 정리 (선택사항)
        return;
    }

    /**
     * 장바구니에서 아이템 제거
     */
    public void removeItemFromCart(Long userId, Long cartItemId) {
        // TODO: 구현 필요
        return;
    }

    /**
     * 사용자의 장바구니 조회
     */
    @Transactional(readOnly = true)
    public Object getUserCart(Long userId) {
        // TODO: 구현 필요
        // 1. CartQueryUseCase.getUserCart 호출
        // 2. 만료된 아이템 표시 (상태 값으로 구분)
        return null;
    }

    /**
     * 장바구니에서 선택 아이템 즉시구매
     */
    public List<String> buyNowFromCart(Long userId, List<Long> cartItemIds) {
        // TODO: 구현 필요
        // 1. CartBuyNowUseCase.buyNowFromCart 호출
        // 2. 생성된 주문 ID 목록 반환
        // 3. 구매완료된 아이템 장바구니에서 제거 (자동 처리됨)
        return null;
    }

    /**
     * 장바구니 전체 즉시구매
     */
    public List<String> buyNowAllCart(Long userId) {
        // TODO: 구현 필요
        // 1. CartBuyNowUseCase.buyNowAllCart 호출
        return null;
    }

    /**
     * 만료된 아이템 일괄 제거
     */
    public void clearExpiredItems(Long userId) {
        // TODO: 구현 필요
        return;
    }

    /**
     * 구매완료 아이템 일괄 제거
     */
    public void clearPurchasedItems(Long userId) {
        // TODO: 구현 필요
        return;
    }

}
