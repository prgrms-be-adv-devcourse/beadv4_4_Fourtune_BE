package com.fourtune.auction.boundedContext.payment.adapter.out.external;

import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.auction.global.config.WebClientConfig;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.auction.dto.OrderDetailResponse;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionAdapter implements AuctionPort {

    private final WebClientConfig webclient;

    // 경매 모듈 API 주소 (application.yml 등에서 관리하는 것이 좋음)
    @Value("${api.auction.base-url}")
    private String BASE_URL;
    private static final String AUCTION_MODULE_URL =
            "/api/v1/orders/public/";

    @Override
    public OrderDto getOrder(String orderNo) {
        try {
            String url = BASE_URL + AUCTION_MODULE_URL + orderNo;

            OrderDetailResponse orderDetailResponse = webclient.webClient()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(OrderDetailResponse.class)
                    .block();

            if (orderDetailResponse == null) {
                throw new BusinessException(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND);
            }

            return OrderDto.from(orderDetailResponse);


        } catch (Exception e) {
            log.error("경매 모듈 연동 실패: {}", e.getMessage());
            // 결제 로직의 안정을 위해 예외를 던져서 트랜잭션을 롤백시키거나 처리해야 함
            throw new BusinessException(ErrorCode.PAYMENT_AUCTION_SERVICE_ERROR);
        }
    }
}
