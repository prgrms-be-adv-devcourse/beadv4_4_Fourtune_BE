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
     * [진입점] 낙찰 주문 생성
     * 동시성 제어: Pessimistic Lock 적용
     */
    @Transactional
    public String createWinningOrder(Long auctionId, Long winnerId, BigDecimal finalPrice) {
        // 1. 경매 조회 (Pessimistic Lock 적용)
        AuctionItem auctionItem = auctionSupport.findByIdWithLockOrThrow(auctionId);
        
        // 2. 내부 메서드로 위임
        return createOrderInternal(auctionItem, winnerId, finalPrice, "[낙찰] " + auctionItem.getTitle());
    }

    /**
     * [진입점] 즉시구매 주문 생성
     * 경매 상태 변경은 AuctionBuyNowUseCase에서 처리함
     * 동시성 제어: Pessimistic Lock 적용
     */
    @Transactional
    public String createBuyNowOrder(Long auctionId, Long buyerId, BigDecimal buyNowPrice) {
        // 1. 경매 조회 (Pessimistic Lock 적용)
        AuctionItem auctionItem = auctionSupport.findByIdWithLockOrThrow(auctionId);
        
        // 2. 내부 메서드로 위임
        return createOrderInternal(auctionItem, buyerId, buyNowPrice, "[즉시구매] " + auctionItem.getTitle());
    }

    /**
     * [진입점] 낙찰 주문 생성 (엔티티 직접 전달)
     * 이미 Lock이 획득된 엔티티를 사용하여 중복 Lock 방지
     * 외부 UseCase에서 이미 Lock을 획득한 경우 사용
     */
    @Transactional
    public String createWinningOrder(AuctionItem auctionItem, Long winnerId, BigDecimal finalPrice) {
        return createOrderInternal(auctionItem, winnerId, finalPrice, "[낙찰] " + auctionItem.getTitle());
    }

    /**
     * [진입점] 즉시구매 주문 생성 (엔티티 직접 전달)
     * 이미 Lock이 획득된 엔티티를 사용하여 중복 Lock 방지
     * 외부 UseCase에서 이미 Lock을 획득한 경우 사용
     */
    @Transactional
    public String createBuyNowOrder(AuctionItem auctionItem, Long buyerId, BigDecimal buyNowPrice) {
        return createOrderInternal(auctionItem, buyerId, buyNowPrice, "[즉시구매] " + auctionItem.getTitle());
    }

    /**
     * [내부 로직] 실제 주문 생성 공통 메서드
     * - private 선언: 외부 호출 방지
     * - @Transactional 제거: 부모 트랜잭션을 그대로 따라감
     * - 공통화: 낙찰/즉시구매 로직이 거의 같으므로 하나로 합침
     */
    private String createOrderInternal(AuctionItem auctionItem, Long buyerId, BigDecimal price, String orderName) {
        // 1. 주문 생성 가능 여부 검증 (낙찰/즉시구매 공통)
        orderSupport.validateOrderCreatable(auctionItem.getId());

        // 2. Order 엔티티 생성
        Order order = Order.create(
                auctionItem.getId(),
                buyerId,
                auctionItem.getSellerId(),
                price,
                orderName
        );

        // 3. 저장 및 반환
        return orderSupport.save(order).getOrderId();
    }

}
