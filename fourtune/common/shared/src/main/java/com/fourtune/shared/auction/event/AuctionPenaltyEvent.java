package com.fourtune.shared.auction.event;

/**
 * 유저 패널티 부여 이벤트 (userId만 전달)
 * 발행처: (미구현 시) 없음. 유저 도메인에서 수신 후 penalty(userId) 호출
 */
public record AuctionPenaltyEvent(Long userId) {}
