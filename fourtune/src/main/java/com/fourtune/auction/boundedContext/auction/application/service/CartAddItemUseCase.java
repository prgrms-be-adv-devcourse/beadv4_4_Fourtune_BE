package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
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
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 즉시구매 가능 여부 확인
        validateBuyNowAvailable(auctionItem);
        
        // 3. 본인 경매에는 담을 수 없음
        if (auctionItem.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_ADD_TO_CART);
        }
        
        // 4. 장바구니 조회 또는 생성
        Cart cart = cartSupport.findOrCreateCart(userId);
        
        // 5. 장바구니에 아이템 추가 (Cart.addItem 메서드에서 중복 체크)
        cart.addItem(auctionId, auctionItem.getBuyNowPrice());
        
        // 6. DB 저장
        cartSupport.save(cart);
    }

    /**
     * 즉시구매 가능 여부 검증
     */
    private void validateBuyNowAvailable(AuctionItem auctionItem) {
        // 1. buyNowEnabled = true
        if (!auctionItem.getBuyNowEnabled()) {
            throw new BusinessException(ErrorCode.BUY_NOW_NOT_ENABLED);
        }
        
        // 2. buyNowPrice != null
        if (auctionItem.getBuyNowPrice() == null) {
            throw new BusinessException(ErrorCode.BUY_NOW_PRICE_NOT_SET);
        }
        
        // 3. status = ACTIVE 만 가능 (SCHEDULED는 아직 시작 안됨)
        if (auctionItem.getStatus() != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
    }

}
