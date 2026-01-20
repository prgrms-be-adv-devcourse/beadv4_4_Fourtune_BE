package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입찰 응답 DTO
 */
public record BidResponse(
    Long id,
    Long auctionId,
    Long bidderId,
    String bidderNickname,
    BigDecimal bidAmount,
    BidStatus status,
    Boolean isWinning,
    LocalDateTime createdAt
) {
    // TODO: from(Bid) 정적 팩토리 메서드 구현
}
