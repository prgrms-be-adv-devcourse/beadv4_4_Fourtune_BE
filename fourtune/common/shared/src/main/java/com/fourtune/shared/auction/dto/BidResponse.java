package com.fourtune.shared.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입찰 응답 DTO
 */
public record BidResponse(
    Long id,
    Long auctionId,
    String auctionTitle,
    Long bidderId,
    String bidderNickname,
    BigDecimal bidAmount,
    String status,
    Boolean isWinning,
    LocalDateTime createdAt
) {
}
