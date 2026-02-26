package com.fourtune.payment.adapter.out.external;

import com.fourtune.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.payment.port.out.PaymentGatewayPort;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.reactive.function.client.WebClientResponseException;

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
            ResponseEntity<Map> response = webClient.post()
                    .uri(TOSS_API_URL + "/confirm")
                    .header("Authorization", basicAuth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(Map.class)
                    .block();

            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return new PaymentExecutionResult(paymentKey, orderId, amount, true);
            } else {
                throw new BusinessException(ErrorCode.PAYMENT_PG_FAILED);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Toss Confirm API 오류 응답: orderId={}, status={}, body={}", orderId, e.getStatusCode(), errorBody);

            // Toss 에러 코드별 분기
            if (errorBody.contains("ALREADY_PROCESSING_REQUEST")) {
                // 동시 confirm 요청 중 하나가 Toss에서 처리 중 → 동시 요청 충돌
                // PaymentConfirmUseCase에서 DB 재확인 후 처리하도록 위임
                log.warn("Toss 동시 confirm 충돌 감지 (ALREADY_PROCESSING_REQUEST). orderId={}", orderId);
                throw new BusinessException(ErrorCode.PAYMENT_PG_SERVER_ERROR);
            }
            if (errorBody.contains("ALREADY_PROCESSED_PAYMENT")) {
                // 이미 승인 완료된 결제 → PaymentConfirmUseCase에서 DB 재확인 후 처리
                throw new BusinessException(ErrorCode.PAYMENT_PG_SERVER_ERROR);
            }
            if (errorBody.contains("PAY_PROCESS_CANCELED") || errorBody.contains("PAY_PROCESS_ABORTED")) {
                // 사용자가 결제 창에서 직접 취소
                throw new BusinessException(ErrorCode.PAYMENT_PG_FAILED);
            }
            if (errorBody.contains("REJECT_CARD_COMPANY")) {
                throw new BusinessException(ErrorCode.PAYMENT_PG_FAILED);
            }
            throw new BusinessException(ErrorCode.PAYMENT_PG_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Toss Confirm API 호출 중 예외 발생: orderId={}, error={}", orderId, e.getMessage());
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
