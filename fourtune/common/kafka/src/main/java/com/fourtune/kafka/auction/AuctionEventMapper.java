package com.fourtune.kafka.auction;

import com.fourtune.shared.auction.event.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka Consumer 역직렬화용: eventType 문자열 → 이벤트 Class 매핑
 */
@Slf4j
public final class AuctionEventMapper {

    private static final Map<String, Class<?>> TYPE_TO_CLASS = new ConcurrentHashMap<>();

    static {
        TYPE_TO_CLASS.put(AuctionEventType.BID_PLACED.name(), BidPlacedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_CREATED.name(), AuctionCreatedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_ITEM_CREATED.name(), AuctionItemCreatedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_ITEM_UPDATED.name(), AuctionItemUpdatedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_ITEM_DELETED.name(), AuctionItemDeletedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_UPDATED.name(), AuctionUpdatedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_STARTED.name(), AuctionStartedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_EXTENDED.name(), AuctionExtendedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_CLOSED.name(), AuctionClosedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_DELETED.name(), AuctionDeletedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.AUCTION_BUY_NOW.name(), AuctionBuyNowEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.BID_CANCELED.name(), BidCanceledEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.ORDER_COMPLETED.name(), OrderCompletedEvent.class);
        TYPE_TO_CLASS.put(AuctionEventType.ORDER_CANCELLED.name(), OrderCancelledEvent.class);
    }

    private AuctionEventMapper() {
    }

    /**
     * eventType 문자열에 해당하는 이벤트 Class 반환 (Consumer 역직렬화 시 사용)
     */
    public static Class<?> getClass(String eventType) {
        Class<?> clazz = TYPE_TO_CLASS.get(eventType);
        if (clazz == null) {
            log.warn("알 수 없는 경매 이벤트 타입: {}", eventType);
            throw new IllegalArgumentException("Unknown auction event type: " + eventType);
        }
        return clazz;
    }
}
