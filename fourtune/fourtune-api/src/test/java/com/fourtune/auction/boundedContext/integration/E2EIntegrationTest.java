package com.fourtune.auction.boundedContext.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E 통합 테스트 (Phase 2 이후 비활성화)
 * <p>
 * 결제 도메인이 payment-service로 분리(Phase 2)되면서, 기존 결제·지갑을 사용하던
 * E2E 시나리오(경매 → 주문 → 결제 확인 → 정산)는 fourtune-api에서 제거되었습니다.
 * 정산 완료 → 지급 흐름은 payment-service의 Kafka 리스너가 구독하여 처리합니다.
 * 결제·정산 지급 검증은 payment-service 및 auction-service 쪽 테스트에서 수행하세요.
 * </p>
 *
 * 문서: docs/msa/PHASE2_PAYMENT_REMOVAL_GUIDE.md
 */
@Disabled("Phase 2: 결제 분리로 인해 E2E 시나리오가 payment-service/auction-service로 이전됨. 필요 시 해당 서비스에서 E2E 검증.")
class E2EIntegrationTest {

    @Test
    @DisplayName("Placeholder: Phase 2 이후 E2E는 payment/auction 서비스에서 검증")
    void placeholder_phase2_payment_removed() {
        // 결제·지갑 제거 후 fourtune-api 단독 E2E는 비활성화.
        // 정산 완료 → 지급은 payment-service의 PaymentSettlementKafkaListener에서 처리.
    }
}
