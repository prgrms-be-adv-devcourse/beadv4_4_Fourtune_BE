package com.fourtune.common.shared.auction.dto;

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
    String orderType,
    String status,
    String paymentKey,
    LocalDateTime paidAt,
    LocalDateTime createdAt
) {

}
