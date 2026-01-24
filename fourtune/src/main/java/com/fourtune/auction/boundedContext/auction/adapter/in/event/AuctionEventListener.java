package com.fourtune.auction.boundedContext.auction.adapter.in.event;

import com.fourtune.auction.boundedContext.auction.application.service.AuctionExtendUseCase;
import com.fourtune.auction.boundedContext.auction.application.service.AuctionSupport;
import com.fourtune.auction.boundedContext.auction.application.service.CartSupport;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.application.service.OrderCompleteUseCase;
import com.fourtune.auction.shared.auction.event.AuctionClosedEvent;
import com.fourtune.auction.shared.auction.event.AuctionCreatedEvent;
import com.fourtune.auction.shared.auction.event.BidPlacedEvent;
import com.fourtune.auction.shared.payment.event.PaymentFailedEvent;
import com.fourtune.auction.shared.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 경매 도메인 이벤트 리스너
 * - 입찰 이벤트 수신 → 자동 연장 체크
 * - 경매 종료 이벤트 수신 → 장바구니 아이템 만료 처리
 * - 결제 완료/실패 이벤트 수신 → 주문 상태 업데이트
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AuctionEventListener {

    private final AuctionSupport auctionSupport;
    private final AuctionExtendUseCase auctionExtendUseCase;
    private final CartSupport cartSupport;
    private final OrderCompleteUseCase orderCompleteUseCase;

    /**
     * 입찰 완료 이벤트 처리
     * - 자동 연장 체크 (BidPlaceUseCase에서 이미 처리하지만, 비동기 추가 처리용)
     * - 실시간 알림 전송 (TODO: NotificationService 연동)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidPlaced(BidPlacedEvent event) {
        log.info("입찰 이벤트 수신: auctionId={}, bidId={}, bidderId={}, amount={}", 
                event.auctionId(), event.bidId(), event.bidderId(), event.bidAmount());
        
        // TODO: 실시간 알림 전송 (WebSocket or SSE)
        // notificationService.notifyBidPlaced(event);
        
        // TODO: 이전 최고 입찰자에게 알림
        // notificationService.notifyOutbid(previousHighestBidderId, event);
    }

    /**
     * 경매 생성 이벤트 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionCreated(AuctionCreatedEvent event) {
        log.info("경매 생성 이벤트 수신: auctionId={}", event.auctionId());
        
        // TODO: Elasticsearch 색인 (SearchService 연동)
        // searchService.indexAuction(event.auctionId());
    }

    /**
     * 경매 종료 이벤트 처리
     * - 장바구니에 담긴 해당 경매 아이템 만료 처리
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionClosed(AuctionClosedEvent event) {
        log.info("경매 종료 이벤트 수신: auctionId={}, winnerId={}, finalPrice={}", 
                event.auctionId(), event.winnerId(), event.finalPrice());
        
        // 1. 장바구니에 담긴 해당 경매 아이템 만료 처리
        try {
            cartSupport.expireCartItemsByAuctionId(event.auctionId());
            log.debug("장바구니 아이템 만료 처리 완료: auctionId={}", event.auctionId());
        } catch (Exception e) {
            log.error("장바구니 아이템 만료 처리 실패: auctionId={}, error={}", 
                    event.auctionId(), e.getMessage());
        }
        
        // TODO: 낙찰자에게 알림
        // if (event.winnerId() != null) {
        //     notificationService.notifyAuctionWon(event.winnerId(), event);
        // }
        
        // TODO: 판매자에게 알림
        // notificationService.notifyAuctionClosed(sellerId, event);
        
        // TODO: Elasticsearch 업데이트
        // searchService.updateAuctionStatus(event.auctionId());
    }

    /**
     * 결제 완료 이벤트 처리
     * - Payment 도메인에서 발행한 PaymentSucceededEvent 수신
     * - 주문 상태를 PENDING → COMPLETED로 변경
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        String orderId = event.getOrder() != null 
                ? String.valueOf(event.getOrder().getOrderId()) 
                : "unknown";
        Long userId = event.getOrder() != null ? event.getOrder().getUserId() : null;
        Long amount = event.getOrder() != null ? event.getOrder().getPrice() : null;
        
        log.info("✅ [결제 완료 이벤트 수신] orderId={}, userId={}, amount={}", 
                orderId, userId, amount);
        
        try {
            // 주문 완료 처리 (PENDING → COMPLETED)
            // paymentKey는 이벤트에 없으므로 빈 문자열로 처리 (필요시 이벤트에 추가 가능)
            orderCompleteUseCase.completePayment(
                    orderId, 
                    ""  // paymentKey는 결제 도메인에서 관리하므로 여기서는 불필요
            );
            log.info("✅ [주문 완료 처리 완료] orderId={}", orderId);
        } catch (Exception e) {
            log.error("❌ [주문 완료 처리 실패] orderId={}, error={}", 
                    orderId, e.getMessage(), e);
            // 주문 완료 실패는 심각한 문제이므로 재시도 로직이나 알림 필요할 수 있음
            // TODO: 재시도 로직 추가 또는 알림 발송
        }
    }

    /**
     * 결제 실패 이벤트 처리
     * - Payment 도메인에서 발행한 PaymentFailedEvent 수신
     * - 주문 상태를 PENDING → CANCELLED로 변경
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        String orderId = event.getOrder() != null 
                ? String.valueOf(event.getOrder().getOrderId()) 
                : "unknown";
        String resultCode = event.getResultCode();
        String message = event.getMsg();
        Long shortfallAmount = event.getShortfallAmount();
        
        log.info("❌ [결제 실패 이벤트 수신] orderId={}, resultCode={}, message={}, shortfallAmount={}", 
                orderId, resultCode, message, shortfallAmount);
        
        try {
            // 주문 상태 변경: PENDING → CANCELLED
            if (event.getOrder() != null) {
                orderCompleteUseCase.failPayment(
                        orderId,
                        message != null ? message : "결제 실패"
                );
                log.info("✅ [주문 취소 처리 완료] orderId={}, reason={}", orderId, message);
            } else {
                log.warn("⚠️ [결제 실패 이벤트 수신] Order 정보가 없어 주문 취소 처리 불가: resultCode={}, message={}", 
                        resultCode, message);
            }
        } catch (Exception e) {
            log.error("❌ [주문 취소 처리 실패] orderId={}, error={}", 
                    orderId, e.getMessage(), e);
            // 주문 취소 실패는 심각한 문제이므로 재시도 로직이나 알림 필요할 수 있음
            // TODO: 재시도 로직 추가 또는 알림 발송
        }
    }
}
