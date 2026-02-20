package com.fourtune.outbox.domain;

/**
 * Outbox 이벤트 상태
 */
public enum OutboxEventStatus {
    PENDING,    // 발행 대기
    PUBLISHED,  // 발행 완료
    FAILED      // 발행 실패
}
