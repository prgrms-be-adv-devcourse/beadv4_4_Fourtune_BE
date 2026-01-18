package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import com.fourtune.auction.boundedContext.auction.domain.entity.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 장바구니 즉시구매 UseCase
 * - 장바구니에서 선택한 상품들 즉시구매
 * - AuctionBuyNowUseCase를 내부적으로 호출
 */
@Service
@RequiredArgsConstructor
public class CartBuyNowUseCase {

    private final CartSupport cartSupport;
    private final AuctionBuyNowUseCase auctionBuyNowUseCase; // 단일 경매 즉시구매 UseCase 사용

    /**
     * 장바구니에서 선택 아이템 즉시구매
     */
    @Transactional
    public List<String> buyNowFromCart(Long userId, List<Long> cartItemIds) {
        // TODO: 구현 필요
        // 1. 장바구니 조회
        // 2. 선택된 CartItem들 조회 및 검증
        //    - 본인의 장바구니인지 확인
        //    - CartItem 상태가 ACTIVE인지 확인
        // 3. 각 CartItem마다:
        //    - AuctionBuyNowUseCase.executeBuyNow(auctionId, userId) 호출
        //    - 즉시구매 성공 시 CartItem 상태 변경 (ACTIVE -> PURCHASED)
        //    - orderId 수집
        // 4. 생성된 주문 ID 목록 반환
        return null;
    }

    /**
     * 장바구니 전체 즉시구매
     */
    @Transactional
    public List<String> buyNowAllCart(Long userId) {
        // TODO: 구현 필요
        // 1. 장바구니 조회
        // 2. 활성 상태의 모든 아이템 조회
        // 3. buyNowFromCart 호출
        return null;
    }

}
