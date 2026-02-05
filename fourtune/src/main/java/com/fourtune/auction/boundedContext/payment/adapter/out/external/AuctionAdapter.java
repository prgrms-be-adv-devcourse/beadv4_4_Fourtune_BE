package com.fourtune.auction.boundedContext.payment.adapter.out.external;

import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.global.config.WebClientConfig;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.auction.dto.OrderDetailResponse;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
    public OrderDto getOrder(String orderId) {
        try {
            String url = BASE_URL + AUCTION_MODULE_URL + orderId;

            ApiResponse<OrderDetailResponse> response = webclient.webClient()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<OrderDetailResponse>>() {})
                    .block();

            // 2. 전체 응답 또는 내부 데이터(data)가 없는 경우 예외 처리
            if (response == null || response.getData() == null) {
                log.error("경매 모듈 응답 데이터 없음 - 주문번호: {}", orderId);
                throw new BusinessException(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND);
            }

            return OrderDto.from(response.getData());


        } catch (Exception e) {
            log.error("경매 모듈 연동 실패: {}", e.getMessage());
            // 결제 로직의 안정을 위해 예외를 던져서 트랜잭션을 롤백시키거나 처리해야 함
            throw new BusinessException(ErrorCode.PAYMENT_AUCTION_SERVICE_ERROR);
        }
    }
}
