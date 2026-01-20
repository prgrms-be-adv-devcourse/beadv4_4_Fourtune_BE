package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.auction.dto.OrderDetailResponse;
import com.fourtune.auction.shared.auction.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 주문 조회 UseCase
 * - 주문 상세 조회
 * - 사용자별 주문 목록 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryUseCase {

    private final OrderSupport orderSupport;
    private final AuctionSupport auctionSupport;

    /**
     * 주문번호로 주문 조회
     */
    public OrderDetailResponse getOrderByOrderId(String orderId) {
        // 1. 주문 조회
        Order order = orderSupport.findByOrderIdOrThrow(orderId);
        
        // 2. 경매 정보 조회
        AuctionItem auctionItem = auctionSupport.findById(order.getAuctionId()).orElse(null);
        
        // 3. DTO 변환 후 반환
        return OrderDetailResponse.from(order, auctionItem);
    }

    /**
     * 주문 ID(PK)로 주문 조회
     */
    public OrderDetailResponse getOrderById(Long id) {
        // 1. 주문 조회
        Order order = orderSupport.findByIdOrThrow(id);
        
        // 2. 경매 정보 조회
        AuctionItem auctionItem = auctionSupport.findById(order.getAuctionId()).orElse(null);
        
        // 3. DTO 변환 후 반환
        return OrderDetailResponse.from(order, auctionItem);
    }

    /**
     * 사용자의 주문 목록 조회 (구매자)
     */
    public List<OrderResponse> getUserOrders(Long winnerId) {
        // 1. 낙찰자 ID로 주문 목록 조회
        List<Order> orders = orderSupport.findByWinnerId(winnerId);
        
        // 2. DTO 변환 후 반환
        return orders.stream()
                .map(order -> {
                    AuctionItem auctionItem = auctionSupport.findById(order.getAuctionId()).orElse(null);
                    return OrderResponse.from(order, auctionItem);
                })
                .toList();
    }

    /**
     * 경매의 주문 조회
     */
    public OrderDetailResponse getOrderByAuctionId(Long auctionId) {
        // 1. 경매 존재 확인
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 주문 조회
        Optional<Order> orderOpt = orderSupport.findByAuctionId(auctionId);
        if (orderOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        // 3. DTO 변환 후 반환
        return OrderDetailResponse.from(orderOpt.get(), auctionItem);
    }

    /**
     * 주문 존재 여부 확인
     */
    public boolean existsByAuctionId(Long auctionId) {
        return orderSupport.existsByAuctionId(auctionId);
    }

}
