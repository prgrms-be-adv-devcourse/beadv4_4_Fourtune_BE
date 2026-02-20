package com.fourtune.shared.kafka.auction;


/**
 * 경매 도메인 Outbox/Kafka 이벤트 타입
 * shared/auction/event 패키지의 이벤트 클래스명과 1:1 매핑 (BidCanceledEvent, OrderCancelledEvent 스펠링 유지)
 */
public enum AuctionEventType {
    BID_PLACED,
    AUCTION_CREATED,
    AUCTION_ITEM_CREATED,
    AUCTION_ITEM_UPDATED,
    AUCTION_ITEM_DELETED,
    AUCTION_UPDATED,
    AUCTION_STARTED,
    AUCTION_EXTENDED,
    AUCTION_CLOSED,
    AUCTION_DELETED,
    AUCTION_BUY_NOW,
    BID_CANCELED,
    ORDER_COMPLETED,
    ORDER_CANCELLED
}
