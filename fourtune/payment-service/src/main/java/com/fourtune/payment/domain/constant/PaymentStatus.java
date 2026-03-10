package com.fourtune.payment.domain.constant;

public enum PaymentStatus {
    /**
     * 지갑 충전 시 PG 호출 전 저장되는 대기 상태.
     * 이 상태로 레코드가 남아있으면 PG 승인은 됐으나 서버 크래시로 DB 처리가 완료되지 않은 것 → 정산 대사 필요.
     */
    CHARGE_PENDING,
    APPROVED,
    CANCELED,
    PARTIAL_CANCELED
}
