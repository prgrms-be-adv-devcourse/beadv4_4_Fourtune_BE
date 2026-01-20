package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 주문 생성 UseCase
 * - 경매 종료 시 낙찰자 주문 생성
 * - 즉시구매 시 주문 생성
 */
@Service
@RequiredArgsConstructor
public class OrderCreateUseCase {

    private final OrderSupport orderSupport;
    private final AuctionSupport auctionSupport;

    /**
     * 낙찰 주문 생성
     */
    @Transactional
    public String createWinningOrder(Long auctionId, Long winnerId, BigDecimal finalPrice) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 이미 주문이 있는지 확인
        // 3. Order 엔티티 생성
        //    - orderId 생성 (UUID or 시간 기반)
        //    - orderType = "AUCTION_WIN"
        //    - status = "PENDING_PAYMENT"
        // 4. DB 저장
        // 5. orderId 반환 (결제 프로세스로 전달)
        return null;
    }

    /**
     * 즉시구매 주문 생성
     */
    @Transactional
    public String createBuyNowOrder(Long auctionId, Long buyerId, BigDecimal buyNowPrice) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 즉시구매 가능 여부 확인
        // 3. Order 엔티티 생성
        //    - orderType = "BUY_NOW"
        //    - status = "PENDING_PAYMENT"
        // 4. 경매 상태 변경 (ACTIVE -> SOLD_BY_BUY_NOW)
        // 5. DB 저장
        // 6. orderId 반환
        return null;
    }

    /**
     * 주문번호 생성
     */
    private String generateOrderId() {
        // TODO: 구현 필요
        // 예: ORD_20260116_123456789
        return null;
    }

}
