package com.fourtune.common.shared.auction.event;

import com.fourtune.common.shared.auction.constant.CancelReason;

/**
 * 주문 취소 이벤트 (패널티 정책용)
 * - 경매 도메인에서 발행, 유저 도메인에서 수신 후 사유에 따라 카운팅·스트라이크 판단
 */
public record OrderCancelledEvent(
    Long userId,         // 패널티 대상자 (주문의 winnerId)
    Long orderId,        // 주문 PK (Order.id)
    Long auctionId,      // 경매 PK (즉시구매 미결제 시 Redis 키: penalty:buynow:{auctionId}:{userId})
    CancelReason reason
) {}
