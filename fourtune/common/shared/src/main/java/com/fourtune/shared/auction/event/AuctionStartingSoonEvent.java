package com.fourtune.shared.auction.event;

/**
 * 경매 시작 5분 전 이벤트
 * - AuctionScheduler에서 발행
 * - WatchList 도메인에서 소비하여 관심유저 알림 생성
 */
public record AuctionStartingSoonEvent(
                Long auctionId) {
}
