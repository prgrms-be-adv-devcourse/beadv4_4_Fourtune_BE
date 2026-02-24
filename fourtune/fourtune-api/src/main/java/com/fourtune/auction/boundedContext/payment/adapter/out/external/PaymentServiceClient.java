package com.fourtune.auction.boundedContext.payment.adapter.out.external;

import com.fourtune.shared.settlement.dto.SettlementDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * payment-service 내부 API 호출용 Feign 클라이언트.
 * 1번 B안 적용 시 PaymentSettlementKafkaListener에서 정산 지급 요청에 사용.
 */
@FeignClient(name = "payment-service", url = "${payment.service.base-url}")
public interface PaymentServiceClient {

    /**
     * 정산 완료 시 payment-service에 지갑 입출금(지급) 요청.
     * payment-service 측 POST /internal/settlement/complete (Phase 1에서 추가됨).
     */
    @PostMapping("/internal/settlement/complete")
    void completeSettlement(@RequestBody SettlementDto dto);
}
