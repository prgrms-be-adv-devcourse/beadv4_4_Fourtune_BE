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
     * 경매 상태 변경은 AuctionBuyNowUseCase에서 처리함
     */
    @Transactional
    public String createBuyNowOrder(Long auctionId, Long buyerId, BigDecimal buyNowPrice) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 주문명 생성
        String orderName = "[즉시구매] " + auctionItem.getTitle();
        
        // 3. Order 엔티티 생성
        Order order = Order.create(
                auctionId,
                buyerId,
                auctionItem.getSellerId(),
                buyNowPrice,
                orderName
        );
        
        // 4. DB 저장
        Order savedOrder = orderSupport.save(order);
        
        // 5. orderId 반환
        return savedOrder.getOrderId();
    }

}
