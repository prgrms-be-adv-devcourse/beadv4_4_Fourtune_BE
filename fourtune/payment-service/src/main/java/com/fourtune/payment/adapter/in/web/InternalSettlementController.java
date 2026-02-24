package com.fourtune.payment.adapter.in.web;

import com.fourtune.payment.application.service.PaymentFacade;
import com.fourtune.shared.settlement.dto.SettlementDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 내부 전용 API (fourtune-api 등 다른 서비스에서만 호출).
 * 정산 완료 시 payment-service에 지갑 입출금(지급) 요청을 받는다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/settlement")
public class InternalSettlementController {

    private final PaymentFacade paymentFacade;

    /**
     * 설정 시 X-Internal-Token 헤더와 일치해야 호출 허용. 비어 있으면 검사 생략(로컬 등).
     */
    @Value("${payment.internal.token:}")
    private String internalToken;

    /**
     * 정산 완료 → 지갑 입출금(지급).
     * fourtune-api의 PaymentSettlementKafkaListener가 settlement-events 수신 후 이 API를 호출한다.
     */
    @PostMapping("/complete")
    public ResponseEntity<Void> completeSettlement(
            @RequestBody SettlementDto dto,
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            HttpServletRequest request
    ) {
        if (isInternalTokenRequired() && !isValidInternalToken(token)) {
            log.warn("내부 API 무단 접근 차단: X-Internal-Token 불일치 또는 없음, path={}", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("정산 지급 요청 수신: payeeId={}, amount={}", dto.getPayeeId(), dto.getAmount());
        paymentFacade.completeSettlement(dto);
        return ResponseEntity.noContent().build();
    }

    private boolean isInternalTokenRequired() {
        return internalToken != null && !internalToken.isBlank();
    }

    private boolean isValidInternalToken(String token) {
        return token != null && token.equals(internalToken);
    }
}
