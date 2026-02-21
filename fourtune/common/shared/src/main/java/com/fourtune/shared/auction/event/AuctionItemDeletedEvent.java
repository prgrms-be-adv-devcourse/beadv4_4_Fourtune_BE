package com.fourtune.shared.auction.event;

/**
 * 경매 아이템 삭제 이벤트 (Search 인덱싱 전용)
 * - 검색 인덱스에서 삭제하기 위한 이벤트
 * - auctionId만 필요 (Search 도메인에서 직접 조회 불필요)
 */
public record AuctionItemDeletedEvent(
    Long auctionItemId
) {
}
