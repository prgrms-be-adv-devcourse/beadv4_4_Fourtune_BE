package com.fourtune.auction.boundedContext.payment.adapter.out.external;

import com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentGatewayPort;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentAdapter implements PaymentGatewayPort {

    @Value("${payment.toss.secret-key}")
    private String tossSecretKey;

    private final WebClient webClient;

    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments";

    /**
     * HTTP POST 요청으로 토스 결제 승인 API 호출
     */
    @Override
    public PaymentExecutionResult confirm(String paymentKey, String orderId, Long amount) {
        String basicAuth = createBasicAuthHeader();

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        try {
            // WebClient를 이용한 승인 요청
            ResponseEntity<Map> response = webClient.post()
                    .uri(TOSS_API_URL + "/confirm")
                    .header("Authorization", basicAuth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(Map.class)
                    .block(); // 동기식 처리를 위해 대기

            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return new PaymentExecutionResult(paymentKey, orderId, amount, true);
            } else {
                throw new BusinessException(ErrorCode.PAYMENT_PG_FAILED);
            }
        } catch (Exception e) {
            log.error("Toss Confirm API 호출 중 에러 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_PG_SERVER_ERROR);
        }
    }

    /**
     * HTTP POST 요청으로 토스 결제 취소 API 호출 (보상 트랜잭션용)
     */
    @Override
    public PaymentExecutionResult cancel(String paymentKey, String reason, Long requestAmount) {
        String basicAuth = createBasicAuthHeader();

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", reason);

        // requestAmount가 존재하면 부분 취소 금액 설정, 없으면 전액 취소 (API 기본 동작)
        if (requestAmount != null) {
            body.put("cancelAmount", requestAmount);
        }

        try {
            // WebClient를 이용한 취소 요청
            webClient.post()
                    .uri(TOSS_API_URL + "/" + paymentKey + "/cancel")
                    .header("Authorization", basicAuth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity() // 바디 데이터가 필요 없을 때 효율적
                    .block();

            String amountLog = (requestAmount != null) ? requestAmount + "원" : "전액";
            log.info("Toss 결제 취소 성공: paymentKey={}, reason={}, amount={}", paymentKey, reason, amountLog);

            return new PaymentExecutionResult(paymentKey, null, requestAmount, true);

        } catch (Exception e) {
            log.error("CRITICAL: Toss 결제 취소 실패 (수동 확인 필요). paymentKey={}, error={}", paymentKey, e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_PG_REFUND_FAILED);
        }
    }

    // Basic Auth 문자열 생성 헬퍼
    private String createBasicAuthHeader() {
        String secretKey = tossSecretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedAuth;
    }
}
