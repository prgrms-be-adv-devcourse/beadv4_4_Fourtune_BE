package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장바구니 아이템 추가 UseCase
 * - 즉시구매 가능한 경매만 추가 가능
 */
@Service
@RequiredArgsConstructor
public class CartAddItemUseCase {

    private final CartSupport cartSupport;
    private final AuctionSupport auctionSupport;

    /**
     * 장바구니에 아이템 추가
     */
    @Transactional
    public void addItemToCart(Long userId, Long auctionId) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 즉시구매 가능 여부 확인
        //    - buyNowEnabled = true
        //    - status = ACTIVE or SCHEDULED
        //    - buyNowPrice != null
        // 3. 장바구니 조회 또는 생성
        // 4. 장바구니에 아이템 추가 (Cart.addItem 메서드 사용)
        //    - 이미 존재하면 예외 발생
        // 5. DB 저장
    }

    /**
     * 즉시구매 가능 여부 검증
     */
    private void validateBuyNowAvailable(AuctionItem auctionItem) {
        // TODO: 구현 필요
        // - buyNowEnabled = true
        // - status = ACTIVE or SCHEDULED
        // - buyNowPrice != null
    }

}
