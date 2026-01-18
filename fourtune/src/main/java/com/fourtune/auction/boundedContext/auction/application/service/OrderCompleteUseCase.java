package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 완료 UseCase
 * - 결제 완료 시 주문 상태 업데이트
 * - Payment 도메인에서 이벤트 수신
 */
@Service
@RequiredArgsConstructor
public class OrderCompleteUseCase {

    private final OrderSupport orderSupport;

    /**
     * 결제 완료 처리
     */
    @Transactional
    public void completePayment(String orderId, String paymentKey) {
        // TODO: 구현 필요
        // 1. 주문 조회
        // 2. 상태 확인 (PENDING_PAYMENT 상태인지)
        // 3. 주문 상태 변경 (PENDING_PAYMENT -> PAID)
        // 4. paymentKey 저장
        // 5. paidAt 업데이트
    }

    /**
     * 결제 실패 처리
     */
    @Transactional
    public void failPayment(String orderId, String failureReason) {
        // TODO: 구현 필요
        // 1. 주문 조회
        // 2. 주문 상태 변경 (PENDING_PAYMENT -> PAYMENT_FAILED)
        // 3. failureReason 저장
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(String orderId) {
        // TODO: 구현 필요
        // 1. 주문 조회
        // 2. 취소 가능 여부 확인
        // 3. 주문 상태 변경 (PAID -> CANCELLED)
    }

}
