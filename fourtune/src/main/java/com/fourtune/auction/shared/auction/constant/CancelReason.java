package com.fourtune.auction.shared.auction.constant;

/**
 * 주문 취소 사유 (패널티 여부 구분용)
 * event 패키지가 아닌 constant에 둠: "무슨 일이 났는지"가 아니라 "취소 사유 코드"이기 때문.
 */
public enum CancelReason {
    /** 낙찰 후 24시간 미결제 → 즉시 패널티 */
    WINNER_PAYMENT_TIMEOUT,
    /** 즉시구매 후 10분 미결제 → 2회 누적 시 패널티 */
    BUY_NOW_PAYMENT_TIMEOUT,
    /** 사용자 단순 변심 등 */
    USER_REQUEST,
    /** 결제 실패 등 (PG/시스템) */
    PAYMENT_FAILED
}
