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
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 이미 주문이 있는지 확인
        orderSupport.validateOrderCreatable(auctionId);
        
        // 3. 주문명 생성
        String orderName = "[낙찰] " + auctionItem.getTitle();
        
        // 4. Order 엔티티 생성 (정적 팩토리 메서드)
        Order order = Order.create(
                auctionId,
                winnerId,
                auctionItem.getSellerId(),
                finalPrice,
                orderName
        );
        
        // 5. DB 저장
        Order savedOrder = orderSupport.save(order);
        
        // 6. orderId 반환 (UUID)
        return savedOrder.getOrderId();
    }

    /**
     * 즉시구매 주문 생성
     */
    @Transactional
    public String createBuyNowOrder(Long auctionId, Long buyerId, BigDecimal buyNowPrice) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 즉시구매 가능 여부 확인 (엔티티 메서드)
        if (!auctionItem.canAddToCart()) {
            throw new com.fourtune.auction.global.error.exception.BusinessException(
                    com.fourtune.auction.global.error.ErrorCode.BUY_NOW_NOT_ENABLED
            );
        }
        
        // 3. 주문명 생성
        String orderName = "[즉시구매] " + auctionItem.getTitle();
        
        // 4. Order 엔티티 생성
        Order order = Order.create(
                auctionId,
                buyerId,
                auctionItem.getSellerId(),
                buyNowPrice,
                orderName
        );
        
        // 5. 경매 상태 변경 (ACTIVE -> SOLD_BY_BUY_NOW)
        auctionItem.executeBuyNow();
        
        // 6. DB 저장
        Order savedOrder = orderSupport.save(order);
        
        // 7. orderId 반환
        return savedOrder.getOrderId();
    }

}
