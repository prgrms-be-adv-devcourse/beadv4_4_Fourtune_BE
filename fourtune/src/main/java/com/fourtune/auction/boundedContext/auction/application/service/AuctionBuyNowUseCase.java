package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.event.AuctionBuyNowEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 즉시구매 UseCase
 * - 단일 경매 즉시구매 처리 (경매 상세 페이지에서 "즉시구매" 버튼)
 * - 경매 즉시 종료
 * - Order 생성
 * - 장바구니의 해당 경매 아이템 만료 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionBuyNowUseCase {

    private final AuctionSupport auctionSupport;
    private final OrderCreateUseCase orderCreateUseCase;
    private final CartSupport cartSupport;
    private final EventPublisher eventPublisher;

    /**
     * 즉시구매 처리
     * 동시성 제어: Pessimistic Lock 적용
     */
    @Transactional
    public String executeBuyNow(Long auctionId, Long buyerId) {
        // 1. 경매 조회 (Pessimistic Lock 적용)
        AuctionItem auctionItem = auctionSupport.findByIdWithLockOrThrow(auctionId);
        
        // 2. 즉시구매 가능 여부 검증
        validateBuyNowAvailable(auctionItem, buyerId);
        
        // 3. 경매 상태 변경 (ACTIVE -> SOLD_BY_BUY_NOW)
        auctionItem.executeBuyNow();
        auctionSupport.save(auctionItem);
        
        // 4. 주문 생성 (엔티티 직접 전달하여 중복 Lock 방지)
        String orderId = orderCreateUseCase.createBuyNowOrder(auctionItem, buyerId, auctionItem.getBuyNowPrice());
        
        // 5. 장바구니의 해당 경매 아이템 만료 처리
        try {
            cartSupport.expireCartItemsByAuctionId(auctionId);
        } catch (Exception e) {
            log.warn("장바구니 아이템 만료 처리 실패: auctionId={}, error={}", auctionId, e.getMessage());
        }
        
        // 6. 이벤트 발행 (AuctionBuyNowEvent)
        eventPublisher.publish(new AuctionBuyNowEvent(
                auctionId,
                auctionItem.getSellerId(),
                buyerId,
                auctionItem.getBuyNowPrice(),
                orderId,
                LocalDateTime.now()
        ));
        
        log.info("즉시구매 처리 완료: auctionId={}, buyerId={}, orderId={}", auctionId, buyerId, orderId);
        
        // 7. orderId 반환 (결제 프로세스로 전달)
        return orderId;
    }

    /**
     * 즉시구매 가능 여부 검증
     */
    private void validateBuyNowAvailable(AuctionItem auctionItem, Long buyerId) {
        // 1. buyNowEnabled = true 인지
        if (!auctionItem.getBuyNowEnabled()) {
            throw new BusinessException(ErrorCode.BUY_NOW_NOT_ENABLED);
        }
        
        // 2. buyNowPrice != null 인지
        if (auctionItem.getBuyNowPrice() == null) {
            throw new BusinessException(ErrorCode.BUY_NOW_PRICE_NOT_SET);
        }
        
        // 3. status = ACTIVE 인지
        if (auctionItem.getStatus() != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        
        // 4. 본인이 판매자가 아닌지
        if (auctionItem.getSellerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.CANNOT_BUY_OWN_ITEM);
        }
    }

}
