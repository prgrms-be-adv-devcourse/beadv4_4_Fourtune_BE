package com.fourtune.common.shared.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입찰 상세 응답 DTO
 */
public record BidDetailResponse(
    Long id,
    Long auctionId,
    String auctionTitle,
    Long bidderId,
    String bidderNickname,
    BigDecimal bidAmount,
    String status,
    Boolean isWinning,
    LocalDateTime createdAt,
    String message // 입찰 성공/실패 메시지
) {

}
