package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import com.fourtune.auction.boundedContext.auction.port.out.OrderRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 주문 공통 조회/검증 기능
 * - Repository 직접 호출
 * - 여러 UseCase에서 재사용되는 공통 로직
 */
@Component
@RequiredArgsConstructor
public class OrderSupport {

    private final OrderRepository orderRepository;

    /**
     * ID로 주문 조회 (Optional)
     */
    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * ID로 주문 조회 (예외 발생)
     */
    public Order findByIdOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * 주문번호로 주문 조회
     */
    public Optional<Order> findByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    /**
     * 주문번호로 주문 조회 (예외 발생)
     */
    public Order findByOrderIdOrThrow(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * 주문 저장
     */
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    /**
     * 경매 ID로 주문 조회
     */
    public Optional<Order> findByAuctionId(Long auctionId) {
        return orderRepository.findByAuctionId(auctionId);
    }

    /**
     * 낙찰자 ID로 주문 목록 조회
     */
    public List<Order> findByWinnerId(Long winnerId) {
        return orderRepository.findByWinnerIdOrderByCreatedAtDesc(winnerId);
    }

    /**
     * 경매에 주문이 이미 존재하는지 확인
     */
    public boolean existsByAuctionId(Long auctionId) {
        return orderRepository.existsByAuctionId(auctionId);
    }

    /**
     * 주문 생성 가능 여부 검증
     * 이미 주문이 존재하면 생성 불가
     */
    public void validateOrderCreatable(Long auctionId) {
        if (existsByAuctionId(auctionId)) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_EXISTS);
        }
    }

}
