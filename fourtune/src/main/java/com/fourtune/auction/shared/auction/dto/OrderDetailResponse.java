package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 상세 응답 DTO
 */
public record OrderDetailResponse(
    Long id,
    String orderId,
    Long auctionId,
    String auctionTitle,
    String thumbnailUrl,
    Long winnerId,
    String winnerNickname,
    Long sellerId,
    String sellerNickname,
    BigDecimal finalPrice,
    OrderType orderType,
    OrderStatus status,
    String paymentKey,
    LocalDateTime paidAt,
    LocalDateTime createdAt
) {
    // TODO: from(Order, AuctionItem, User, User) 정적 팩토리 메서드 구현
}
