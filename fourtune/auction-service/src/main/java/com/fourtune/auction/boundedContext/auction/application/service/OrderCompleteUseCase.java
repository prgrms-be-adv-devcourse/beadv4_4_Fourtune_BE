package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import com.fourtune.auction.boundedContext.auction.port.out.BidRepository;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.auction.constant.CancelReason;
import com.fourtune.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.shared.auction.event.OrderCancelledEvent;
import com.fourtune.shared.auction.event.OrderCompletedEvent;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final BidRepository bidRepository;
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
     * 결제 재시도용 orderId 갱신
     * Toss는 한 번 시도한 orderId를 재사용 불허하므로, 결제 실패 후 재시도 시 새 orderId 발급
     */
    @Transactional
    public String renewPaymentId(String orderId, Long userId) {
        Order order = orderSupport.findByOrderIdOrThrow(orderId);

        if (!order.getWinnerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED);
        }

        String newOrderId = order.renewOrderId();
        orderSupport.save(order);

        log.info("결제 재시도 orderId 갱신: oldOrderId={}, newOrderId={}, userId={}", orderId, newOrderId, userId);
        return newOrderId;
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
     * - 복구 후 Elasticsearch 인덱스 동기화를 위해 AuctionItemUpdatedEvent 발행
     */
    private void cancelOrderInternal(Order order) {
        order.cancel();
        orderSupport.save(order);

        auctionSupport.findById(order.getAuctionId()).ifPresent(auction -> {
            if (auction.getStatus() == AuctionStatus.SOLD_BY_BUY_NOW) {
                // 즉시구매 시도 전 currentPrice 복원: ACTIVE 상태 최고 입찰가 or 시작가
                BigDecimal previousPrice = bidRepository
                        .findTopByAuctionIdAndStatusOrderByBidAmountDesc(auction.getId(), BidStatus.ACTIVE)
                        .map(bid -> bid.getBidAmount())
                        .orElse(auction.getStartPrice());
                auction.recoverFromBuyNowFailure(
                        AuctionPolicy.BUY_NOW_RECOVERY_EXTEND_MINUTES,
                        AuctionPolicy.BUY_NOW_RECOVERY_MAX_PER_AUCTION,
                        previousPrice
                );
                auctionSupport.save(auction);
                log.info("주문 취소로 경매 복구: auctionId={}, orderId={}, restoredPrice={}, buyNowRecoveryCount={}, buyNowDisabled={}",
                        auction.getId(), order.getOrderId(), previousPrice, auction.getBuyNowRecoveryCount(), auction.getBuyNowDisabledByPolicy());
                publishAuctionItemUpdatedEvent(auction);
            } else if (auction.getStatus() == AuctionStatus.SOLD) {
                auction.fail();
                auctionSupport.save(auction);
                log.info("낙찰 미결제로 유찰 처리: auctionId={}, orderId={}", auction.getId(), order.getOrderId());
                publishAuctionItemUpdatedEvent(auction);
            }
        });
    }

    /**
     * 경매 상태 변경 후 Elasticsearch 인덱스 동기화 이벤트 발행
     * sellerName은 Feign 의존성 없이 null로 처리 (검색 인덱스의 status 필드 갱신이 목적)
     */
    private void publishAuctionItemUpdatedEvent(AuctionItem auction) {
        try {
            String thumbnailUrl = null;
            if (auction.getImages() != null) {
                thumbnailUrl = auction.getImages().stream()
                        .filter(ItemImage::getIsThumbnail)
                        .findFirst()
                        .map(ItemImage::getImageUrl)
                        .orElse(null);
            }
            AuctionItemUpdatedEvent event = new AuctionItemUpdatedEvent(
                    auction.getId(),
                    auction.getSellerId(),
                    null,
                    auction.getTitle(),
                    auction.getDescription(),
                    auction.getCategory().toString(),
                    auction.getStatus().toString(),
                    auction.getStartPrice(),
                    auction.getCurrentPrice(),
                    auction.getBuyNowPrice(),
                    auction.getBuyNowEnabled(),
                    auction.getAuctionStartTime(),
                    auction.getAuctionEndTime(),
                    thumbnailUrl,
                    auction.getCreatedAt(),
                    auction.getUpdatedAt(),
                    auction.getViewCount(),
                    auction.getBidCount(),
                    auction.getWatchlistCount()
            );
            if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
                outboxService.append(AGGREGATE_TYPE_AUCTION, auction.getId(),
                        AuctionEventType.AUCTION_ITEM_UPDATED.name(),
                        Map.of("eventType", AuctionEventType.AUCTION_ITEM_UPDATED.name(),
                                "aggregateId", auction.getId(), "data", event));
            } else {
                eventPublisher.publish(event);
            }
        } catch (Exception e) {
            log.warn("경매 상태 변경 후 검색 인덱스 업데이트 이벤트 발행 실패 (무시): auctionId={}, error={}",
                    auction.getId(), e.getMessage());
        }
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
