package com.fourtune.payment.adapter.out.external;

import com.fourtune.payment.port.out.AuctionPort;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.payment.dto.OrderDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionAdapter implements AuctionPort {

    private final AuctionFeignClient auctionFeignClient;

    @Override
    @CircuitBreaker(name = "auction", fallbackMethod = "getOrderFallback")
    @Retry(name = "auction")
    public OrderDto getOrder(String orderId) {
        try {
            var response = auctionFeignClient.getOrder(orderId);

            if (response == null || response.getData() == null) {
                log.error("경매 모듈 응답 데이터 없음 - 주문번호: {}", orderId);
                throw new BusinessException(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND);
            }

            return OrderDto.from(response.getData());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("경매 모듈 연동 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_AUCTION_SERVICE_ERROR);
        }
    }

    /**
     * Circuit Breaker Open 또는 연속 실패 시 호출. 호출부에 비즈니스 예외 전달.
     */
    public OrderDto getOrderFallback(String orderId, Exception ex) {
        log.warn("경매(주문) 조회 fallback: orderId={}, cause={}", orderId, ex.getMessage());
        throw new BusinessException(ErrorCode.PAYMENT_AUCTION_SERVICE_ERROR);
    }
}