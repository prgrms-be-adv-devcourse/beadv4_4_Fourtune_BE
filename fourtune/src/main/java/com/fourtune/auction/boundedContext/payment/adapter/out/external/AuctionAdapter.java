package com.fourtune.auction.boundedContext.payment.adapter.out.external;

import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.auction.global.config.WebClientConfig;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionAdapter implements AuctionPort {

    // 만약 경매 모듈이 같은 서버 내의 다른 패키지(Service)라면 그것을 주입받아 사용하고,
    // MSA 환경이라서 HTTP 통신을 해야 한다면 RestTemplate이나 FeignClient를 사용합니다.
    // 여기서는 질문 주신 내용("API로 조회")에 맞춰 HTTP 통신 스타일로 예시를 작성합니다.

    private final WebClientConfig webclient;

    // 경매 모듈 API 주소 (application.yml 등에서 관리하는 것이 좋음)
    private static final String AUCTION_MODULE_URL = "http://localhost:8080/api/internal/auction/orders/";

    @Override
    public OrderDto getOrder(Long orderId) {
        try {
            // 예시: GET /api/internal/auction/orders/{orderId}
            String url = AUCTION_MODULE_URL + orderId;

            OrderDto orderDto = webclient.webClient()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(OrderDto.class)
                    .block();

            if (orderDto == null) {
                throw new RuntimeException("경매 주문 정보를 찾을 수 없습니다. orderId=" + orderId);
            }

            return orderDto;

        } catch (Exception e) {
            log.error("경매 모듈 연동 실패: {}", e.getMessage());
            // 결제 로직의 안정을 위해 예외를 던져서 트랜잭션을 롤백시키거나 처리해야 함
            throw new RuntimeException("경매 모듈 조회 중 오류 발생");
        }
    }
}