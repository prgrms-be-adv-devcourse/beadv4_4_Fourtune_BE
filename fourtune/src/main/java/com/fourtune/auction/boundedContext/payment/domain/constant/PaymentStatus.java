package com.fourtune.auction.boundedContext.payment.domain.constant;

public enum PaymentStatus {
    APPROVED, CANCELED, PARTIAL_CANCELED, FAILED
    // 최종 승인, 환불, 부분 환불, 실패(검증or지갑 금액부족)
}
