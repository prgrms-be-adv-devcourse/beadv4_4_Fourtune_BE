package com.fourtune.auction.boundedContext.auction.adapter.in.scheduler;

import com.fourtune.auction.boundedContext.auction.application.service.OrderCompleteUseCase;
import com.fourtune.auction.boundedContext.auction.application.service.OrderSupport;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 주문 스케줄러
 * - 결제 대기(PENDING) 주문 자동 만료: 즉시구매 10분 / 낙찰 24시간 (ORDER_PAYMENT_POLICY)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderSupport orderSupport;
    private final OrderCompleteUseCase orderCompleteUseCase;

    /**
     * 즉시구매 결제 대기 자동 만료 시간 (분)
     * ORDER_PAYMENT_POLICY: 즉시구매 10분 유예 → 10분 연장 정책과 연동
     */
    @Value("${order.buy-now-pending-timeout-minutes:10}")
    private int buyNowPendingTimeoutMinutes;

    /**
     * 낙찰 결제 대기 자동 만료 시간 (시간)
     * ORDER_PAYMENT_POLICY: 낙찰 24시간 유예
     */
    @Value("${order.bid-pending-timeout-hours:24}")
    private int bidPendingTimeoutHours;

    /**
     * 만료된 PENDING 주문 자동 취소
     * 매 분 0초 실행 (즉시구매 10분 유예 정책 준수를 위해 1분 단위 실행)
     * - 즉시구매: 10분 초과 시 취소 + 경매 복구(Soft Closing)
     * - 낙찰: 24시간 초과 시 취소
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void cancelExpiredPendingOrders() {
        log.info("만료된 결제 대기 주문 정리 작업 시작 (즉시구매={}분, 낙찰={}시간)",
                buyNowPendingTimeoutMinutes, bidPendingTimeoutHours);

        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            LocalDateTime buyNowCutoff = now.minusMinutes(buyNowPendingTimeoutMinutes);
            LocalDateTime bidCutoff = now.minusHours(bidPendingTimeoutHours);

            List<Order> expiredBuyNow = orderSupport.findExpiredPendingBuyNowOrders(buyNowCutoff);
            List<Order> expiredBid = orderSupport.findExpiredPendingBidOrders(bidCutoff);

            int cancelledCount = 0;
            for (Order order : expiredBuyNow) {
                try {
                    orderCompleteUseCase.cancelExpiredOrder(order.getOrderId());
                    cancelledCount++;
                } catch (Exception e) {
                    log.error("만료 즉시구매 주문 취소 실패: orderId={}, error={}", order.getOrderId(), e.getMessage(), e);
                }
            }
            for (Order order : expiredBid) {
                try {
                    orderCompleteUseCase.cancelExpiredOrder(order.getOrderId());
                    cancelledCount++;
                } catch (Exception e) {
                    log.error("만료 낙찰 주문 취소 실패: orderId={}, error={}", order.getOrderId(), e.getMessage(), e);
                }
            }

            log.info("만료된 결제 대기 주문 정리 완료: {}건 취소 (대상: 즉시구매 {}건, 낙찰 {}건)",
                    cancelledCount, expiredBuyNow.size(), expiredBid.size());
        } catch (Exception e) {
            log.error("만료된 결제 대기 주문 정리 중 오류: {}", e.getMessage(), e);
        }
    }
}
