package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderType;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 응답 DTO
 */
public record OrderResponse(
    Long id,
    String orderId,
    Long auctionId,
    String auctionTitle,
    Long winnerId,
    String winnerNickname,
    BigDecimal finalPrice,
    OrderType orderType,
    OrderStatus status,
    LocalDateTime createdAt
) {
    /**
     * Order와 AuctionItem으로 OrderResponse 생성
     */
    public static OrderResponse from(Order order, AuctionItem auctionItem) {
        return new OrderResponse(
                order.getId(),
                order.getOrderId(),
                order.getAuctionId(),
                auctionItem != null ? auctionItem.getTitle() : null,
                order.getWinnerId(),
                null, // winnerNickname은 User 조회 필요
                order.getAmount(),
                determineOrderType(auctionItem),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
    
    /**
     * Order만으로 OrderResponse 생성 (auctionTitle 없음)
     */
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderId(),
                order.getAuctionId(),
                null,
                order.getWinnerId(),
                null,
                order.getAmount(),
                null,
                order.getStatus(),
                order.getCreatedAt()
        );
    }
    
    /**
     * 주문 타입 결정 (경매 낙찰 or 즉시구매)
     */
    private static OrderType determineOrderType(AuctionItem auctionItem) {
        if (auctionItem == null) {
            return OrderType.AUCTION_WIN;
        }
        return switch (auctionItem.getStatus()) {
            case SOLD_BY_BUY_NOW -> OrderType.BUY_NOW;
            default -> OrderType.AUCTION_WIN;
        };
    }
}
