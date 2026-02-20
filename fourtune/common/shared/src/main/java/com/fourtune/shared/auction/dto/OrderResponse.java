package com.fourtune.shared.auction.dto;

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
    String orderType,
    String status,
    LocalDateTime createdAt
) {

}
