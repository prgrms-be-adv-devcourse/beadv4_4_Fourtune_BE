package com.fourtune.auction.boundedContext.payment.adapter.out.external;

import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.core.dto.ApiResponse;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.auction.dto.OrderDetailResponse;
import com.fourtune.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionAdapter implements AuctionPort {

    private final WebClient webClient;

    // 경매 모듈 API 주소 (application.yml 등에서 관리하는 것이 좋음)
    @Value("${api.auction.base-url}")
    private String BASE_URL;
    private static final String AUCTION_MODULE_URL = "/api/v1/orders/public/";

    @Override
    public OrderDto getOrder(String orderId) {
        try {
            String url = BASE_URL + AUCTION_MODULE_URL + orderId;

            ApiResponse<OrderDetailResponse> response = webClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<OrderDetailResponse>>() {
                    })
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
