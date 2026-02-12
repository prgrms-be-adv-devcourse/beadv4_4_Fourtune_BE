package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import com.fourtune.auction.global.config.EventPublishingConfig;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.global.outbox.service.OutboxService;
import com.fourtune.auction.shared.auction.constant.CancelReason;
import com.fourtune.auction.shared.auction.event.OrderCancelledEvent;
import com.fourtune.auction.shared.auction.event.OrderCompletedEvent;
import com.fourtune.auction.shared.auction.kafka.AuctionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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

    private static final String AGGREGATE_TYPE_AUCTION = "Auction";

    private final OrderSupport orderSupport;
    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

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
        Long auctionId = order.getAuctionId();
        OrderCompletedEvent completedEvent = new OrderCompletedEvent(
                order.getOrderId(),
                auctionId,
                order.getWinnerId(),
                order.getSellerId(),
                order.getAmount(),
                order.getOrderName(),
                order.getPaidAt()
        );
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, auctionId, AuctionEventType.ORDER_COMPLETED.name(), Map.of("eventType", AuctionEventType.ORDER_COMPLETED.name(), "aggregateId", auctionId, "data", completedEvent));
        } else {
            eventPublisher.publish(completedEvent);
        }

        log.info("결제 완료 처리: orderId={}, paymentKey={}", orderId, paymentKey);
    }

    /**
     * 결제 실패 처리
     * 주문 취소 + 즉시구매 경매 복구 동일 적용 (cancelOrderInternal)
     */
    @Transactional
    public void failPayment(String orderId, String failureReason) {
        Order order = orderSupport.findByOrderIdOrThrow(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED);
        }
        cancelOrderInternal(order);
        log.info("결제 실패 처리: orderId={}, reason={}", orderId, failureReason);
    }

    /**
     * [진입점] 주문 취소 (orderId 기준)
     */
    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderSupport.findByOrderIdOrThrow(orderId);
        cancelOrderInternal(order);
        log.info("주문 취소 처리: orderId={}", orderId);
    }

    /**
     * [진입점] 주문 취소 (id 기준, 사용자 검증 포함)
     */
    @Transactional
    public void cancelOrderById(Long id, Long userId) {
        Order order = orderSupport.findByIdOrThrow(id);
        if (!order.getWinnerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        cancelOrderInternal(order);
        log.info("주문 취소 처리: id={}, userId={}", id, userId);
    }

    /**
     * [내부 로직] 실제 주문 취소 처리
     * - 주문 취소 후 즉시구매 경매(SOLD_BY_BUY_NOW)면 경매를 ACTIVE로 복구
     */
    private void cancelOrderInternal(Order order) {
        order.cancel();
        orderSupport.save(order);

        auctionSupport.findById(order.getAuctionId()).ifPresent(auction -> {
            if (auction.getStatus() == AuctionStatus.SOLD_BY_BUY_NOW) {
                auction.recoverFromBuyNowFailure(
                        AuctionPolicy.BUY_NOW_RECOVERY_EXTEND_MINUTES,
                        AuctionPolicy.BUY_NOW_RECOVERY_MAX_PER_AUCTION
                );
                auctionSupport.save(auction);
                log.info("주문 취소로 경매 복구: auctionId={}, orderId={}, buyNowRecoveryCount={}, buyNowDisabled={}",
                        auction.getId(), order.getOrderId(), auction.getBuyNowRecoveryCount(), auction.getBuyNowDisabledByPolicy());
            } else if (auction.getStatus() == AuctionStatus.SOLD) {
                auction.fail();
                auctionSupport.save(auction);
                log.info("낙찰 미결제로 유찰 처리: auctionId={}, orderId={}", auction.getId(), order.getOrderId());
            }
        });
    }

    /**
     * 만료된 PENDING 주문 취소 (스케줄러에서 호출)
     * 문서: 취소 사유 결정 → cancelOrderInternal → OrderCancelledEvent 발행 (유저 도메인에서 수신 후 패널티 판단)
     */
    @Transactional
    public void cancelExpiredOrder(String orderId) {
        Order order = orderSupport.findByOrderIdOrThrow(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }
        // 1. 취소 사유 결정 (cancelOrderInternal 호출 전에 경매 상태로 구분)
        CancelReason reason = resolveCancelReasonForExpired(order);
        // 2. 기존대로 취소 + 경매 복구/유찰
        cancelOrderInternal(order);
        // 3. 제보: 취소 사유를 담아 OrderCancelledEvent 발행
        Long auctionId = order.getAuctionId();
        OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(
                order.getWinnerId(),
                order.getId(),
                auctionId,
                reason
        );
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, auctionId, AuctionEventType.ORDER_CANCELLED.name(), Map.of("eventType", AuctionEventType.ORDER_CANCELLED.name(), "aggregateId", auctionId, "data", cancelledEvent));
        } else {
            eventPublisher.publish(cancelledEvent);
        }
        log.info("만료된 주문 자동 취소: orderId={}, reason={}", orderId, reason);
    }

    /**
     * 만료 자동 취소 시 사유 결정 (즉시구매 vs 낙찰)
     * 경매 상태는 cancelOrderInternal 호출 전에 조회 (호출 후에는 이미 ACTIVE/FAIL로 바뀜)
     */
    private CancelReason resolveCancelReasonForExpired(Order order) {
        return auctionSupport.findById(order.getAuctionId())
                .filter(a -> a.getStatus() == AuctionStatus.SOLD_BY_BUY_NOW)
                .isPresent()
                ? CancelReason.BUY_NOW_PAYMENT_TIMEOUT
                : CancelReason.WINNER_PAYMENT_TIMEOUT;
    }
}
