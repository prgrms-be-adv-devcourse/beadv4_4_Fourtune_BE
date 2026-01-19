package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderType;

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
    // TODO: from(Order, AuctionItem, User) 정적 팩토리 메서드 구현
}
