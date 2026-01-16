package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmUseCase {

        final private PaymentCashCompleteUseCase paymentCashCompleteUseCase;
        @Value("${payment.toss.secret-key}")
        private String tossSecretKey;

        private final RestTemplate restTemplate = new RestTemplate();

        public void confirmPayment(String paymentKey, String orderId, Long amount) {
                // 1. 헤더 설정
                String secretKey = tossSecretKey + ":";
                String encodedAuth = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Basic " + encodedAuth);
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 2. 바디 설정
                Map<String, Object> body = new HashMap<>();
                body.put("paymentKey", paymentKey);
                body.put("orderId", orderId);
                body.put("amount", amount);

                // Spring의 HttpEntity 사용
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                // 3. 토스 API 호출
                String url = "https://api.tosspayments.com/v1/payments/confirm";

                ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

                // 4. 응답 확인
                if (response.getStatusCode() != HttpStatus.OK) {
                        throw new RuntimeException("토스 결제 승인 실패: " + response.getBody());
                }

                if(response.getStatusCode().equals(HttpStatus.OK)){
                        // TODO: 주문 완료 처리, cash log 생성(구매자 지갑 -> 시스템 지갑으로 현금 이동)
                        log.info("주문 확인 응답: "+response.getStatusCode().toString());

                        OrderDto orderDto = getOrdrerDto(orderId);
                        if(orderDto.getPrice() == amount){// 존재하고 같은 주문인지 판단
                                // 현금이동
                                paymentCashCompleteUseCase.cashComplete(orderDto, amount);
                        }

                }
        }

        private OrderDto getOrdrerDto(String orderId) {
                return new OrderDto();//TODO: auction에서 요청 받아오기
        }
}