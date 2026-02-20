package com.fourtune.common.shared.auction.event;

/**
 * 경매 종료 5분 전 이벤트
 * - AuctionScheduler에서 발행
 * - WatchList 도메인에서 소비하여 관심유저 알림 생성
 */
public record AuctionEndingSoonEvent(
        Long auctionId
) {
}
