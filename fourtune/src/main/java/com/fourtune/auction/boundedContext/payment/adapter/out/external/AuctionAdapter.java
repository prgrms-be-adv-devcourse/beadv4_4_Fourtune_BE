package com.fourtune.auction.boundedContext.payment.adapter.out.external;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderType;
import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.auction.global.config.WebClientConfig;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.auction.dto.OrderDetailResponse;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
//            // 예시: GET /api/internal/auction/orders/{orderId}
//            String url = AUCTION_MODULE_URL + orderNo;
//
//            OrderDto orderDto = webclient.webClient()
//                    .get()
//                    .uri(url)
//                    .retrieve()
//                    .bodyToMono(OrderDto.class)
//                    .block();
//
//            if (orderDto == null) {
//                throw new BusinessException(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND);
//            }

//            return orderDto;
            OrderDetailResponse orderDetailResponse = new OrderDetailResponse(
                                    1L,                             // id
                                    "ORDER-TEST-0001",              // orderId
                                    1L,                             // auctionId
                                    "테스트 아이템",                  // auctionTitle
                                    "http://localhost:8080/img",    // thumbnailUrl
                                    1L,                             // winnerId
                                    "구매자닉네임",                   // winnerNickname
                                    2L,                             // sellerId
                                    "판매자닉네임",                   // sellerNickname
                                    BigDecimal.valueOf(5000),       // finalPrice
                                    null,                           // orderType
                                    OrderStatus.PENDING,          // status
                                    null,             // paymentKey
                                    null,            // paidAt
                                    LocalDateTime.now()             // createdAt
                            );
            return OrderDto.from(orderDetailResponse);


        } catch (Exception e) {
            log.error("경매 모듈 연동 실패: {}", e.getMessage());
            // 결제 로직의 안정을 위해 예외를 던져서 트랜잭션을 롤백시키거나 처리해야 함
            throw new BusinessException(ErrorCode.PAYMENT_AUCTION_SERVICE_ERROR);
        }
    }
}
