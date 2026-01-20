package com.fourtune.auction.boundedContext.auction.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * 주문번호로 주문 조회
     */
    public Object getOrderByOrderId(String orderId) {
        // TODO: 구현 필요
        // 1. 주문 조회
        // 2. DTO 변환 후 반환
        return null;
    }

    /**
     * 주문 ID로 주문 조회
     */
    public Object getOrderById(Long id) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 사용자의 주문 목록 조회
     */
    public List<Object> getUserOrders(Long winnerId) {
        // TODO: 구현 필요
        // 1. 낙찰자 ID로 주문 목록 조회
        // 2. DTO 변환 후 반환
        return null;
    }

    /**
     * 경매의 주문 조회
     */
    public Object getOrderByAuctionId(Long auctionId) {
        // TODO: 구현 필요
        return null;
    }

}
