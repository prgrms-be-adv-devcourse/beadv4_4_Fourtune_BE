package com.fourtune.common.shared.auction.event;

/**
 * 경매 삭제 이벤트
 * - 경매 삭제 시 발행
 * - 검색 인덱스 삭제에 사용
 */
public record AuctionDeletedEvent(
    Long auctionId,
    Long sellerId,
    String title,
    String category
) {
}
