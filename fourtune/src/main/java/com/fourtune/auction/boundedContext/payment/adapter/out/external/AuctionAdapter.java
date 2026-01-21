package com.fourtune.auction.boundedContext.payment.adapter.out.external;

import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.auction.global.config.WebClientConfig;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionAdapter implements AuctionPort {

    private final WebClientConfig webclient;

    // 경매 모듈 API 주소 (application.yml 등에서 관리하는 것이 좋음)
    private static final String AUCTION_MODULE_URL = "http://localhost:8080/api/internal/auction/orders/";

    @Override
    public OrderDto getOrder(String orderNo) {
        try {
            // 예시: GET /api/internal/auction/orders/{orderId}
            String url = AUCTION_MODULE_URL + orderNo;

            OrderDto orderDto = webclient.webClient()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(OrderDto.class)
                    .block();

            if (orderDto == null) {
                throw new BusinessException(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND);
            }

            return orderDto;

        } catch (Exception e) {
            log.error("경매 모듈 연동 실패: {}", e.getMessage());
            // 결제 로직의 안정을 위해 예외를 던져서 트랜잭션을 롤백시키거나 처리해야 함
            throw new BusinessException(ErrorCode.PAYMENT_AUCTION_SERVICE_ERROR);
        }
    }
}