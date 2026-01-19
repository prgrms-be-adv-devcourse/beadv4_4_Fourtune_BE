package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 즉시구매 UseCase
 * - 단일 경매 즉시구매 처리 (경매 상세 페이지에서 "즉시구매" 버튼)
 * - 경매 즉시 종료
 * - Order 생성
 * - 장바구니의 해당 경매 아이템 만료 처리
 */
@Service
@RequiredArgsConstructor
public class AuctionBuyNowUseCase {

    private final AuctionSupport auctionSupport;
    private final OrderCreateUseCase orderCreateUseCase;
    private final CartSupport cartSupport;
    private final EventPublisher eventPublisher;

    /**
     * 즉시구매 처리
     */
    @Transactional
    public String executeBuyNow(Long auctionId, Long buyerId) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 즉시구매 가능 여부 검증
        //    - buyNowEnabled = true
        //    - status = ACTIVE
        //    - buyNowPrice != null
        //    - 본인이 판매자가 아닌지
        // 3. 경매 상태 변경 (ACTIVE -> SOLD_BY_BUY_NOW)
        // 4. 주문 생성 (OrderCreateUseCase.createBuyNowOrder)
        // 5. 장바구니의 해당 경매 아이템 만료 처리
        //    - CartSupport.expireCartItemsByAuctionId(auctionId)
        // 6. 이벤트 발행 (AuctionBuyNowEvent)
        // 7. orderId 반환 (결제 프로세스로 전달)
        return null;
    }

    /**
     * 즉시구매 가능 여부 검증
     */
    private void validateBuyNowAvailable(AuctionItem auctionItem, Long buyerId) {
        // TODO: 구현 필요
        // - buyNowEnabled = true 인지
        // - status = ACTIVE 인지
        // - buyNowPrice != null 인지
        // - 본인이 판매자가 아닌지
        // - 경매 시작 시간이 되었는지
    }

}
