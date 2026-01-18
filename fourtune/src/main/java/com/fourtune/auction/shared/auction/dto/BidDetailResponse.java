package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;

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
    BidStatus status,
    Boolean isWinning,
    LocalDateTime createdAt,
    String message // 입찰 성공/실패 메시지
) {
    // TODO: from(Bid, AuctionItem) 정적 팩토리 메서드 구현
}
