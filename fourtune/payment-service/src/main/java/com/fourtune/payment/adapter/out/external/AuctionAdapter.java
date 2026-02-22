package com.fourtune.payment.adapter.out.external;

import com.fourtune.payment.port.out.AuctionPort;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionAdapter implements AuctionPort {

    private final AuctionFeignClient auctionFeignClient;

    @Override
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
}