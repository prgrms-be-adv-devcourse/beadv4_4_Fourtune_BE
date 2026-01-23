package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 완료 UseCase
 * - 결제 완료 시 주문 상태 업데이트
 * - Payment 도메인에서 이벤트 수신
 * - 주문 완료 이벤트 발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCompleteUseCase {

    private final OrderSupport orderSupport;
    private final EventPublisher eventPublisher;

    /**
     * 결제 완료 처리
     */
    @Transactional
    public void completePayment(String orderId, String paymentKey) {
        // 1. 주문 조회
        Order order = orderSupport.findByOrderIdOrThrow(orderId);
        
        // 2. 상태 확인 (PENDING 상태인지)
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED);
        }
        
        // 3. 주문 상태 변경 (PENDING -> COMPLETED)
        order.complete();
        
        // 4. 저장
        orderSupport.save(order);
        
        // 5. 이벤트 발행 (정산, 알림 등에 사용)
        eventPublisher.publish(new OrderCompletedEvent(
                order.getOrderId(),
                order.getAuctionId(),
                order.getWinnerId(),
                order.getSellerId(),
                order.getAmount(),
                order.getOrderName(),
                order.getPaidAt()
        ));
        
        log.info("결제 완료 처리: orderId={}, paymentKey={}", orderId, paymentKey);
    }

    /**
     * 결제 실패 처리
     */
    @Transactional
    public void failPayment(String orderId, String failureReason) {
        // 1. 주문 조회
        Order order = orderSupport.findByOrderIdOrThrow(orderId);
        
        // 2. 상태 확인 (PENDING 상태인지)
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED);
        }
        
        // 3. 주문 취소 처리
        order.cancel();
        
        // 4. 저장
        orderSupport.save(order);
        
        log.info("결제 실패 처리: orderId={}, reason={}", orderId, failureReason);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(String orderId) {
        // 1. 주문 조회
        Order order = orderSupport.findByOrderIdOrThrow(orderId);
        
        // 2. 주문 취소 (Order 엔티티에서 취소 가능 여부 검증)
        order.cancel();
        
        // 3. 저장
        orderSupport.save(order);
        
        log.info("주문 취소 처리: orderId={}", orderId);
    }

    /**
     * 주문 취소 (주문 ID 기준)
     */
    @Transactional
    public void cancelOrderById(Long id, Long userId) {
        // 1. 주문 조회
        Order order = orderSupport.findByIdOrThrow(id);
        
        // 2. 본인 확인 (구매자)
        if (!order.getWinnerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 3. 주문 취소
        order.cancel();
        
        // 4. 저장
        orderSupport.save(order);
        
        log.info("주문 취소 처리: id={}, userId={}", id, userId);
    }

}
