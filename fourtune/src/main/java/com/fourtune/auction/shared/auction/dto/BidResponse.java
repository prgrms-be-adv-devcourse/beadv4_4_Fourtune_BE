package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;

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
    /**
     * Bid 엔티티에서 BidResponse 생성
     */
    public static BidResponse from(Bid bid) {
        return new BidResponse(
                bid.getId(),
                bid.getAuctionId(),
                bid.getBidderId(),
                null, // bidderNickname은 User 조회 필요 - 나중에 조인 또는 별도 조회
                bid.getBidAmount(),
                bid.getStatus(),
                bid.getIsWinning(),
                bid.getCreatedAt()
        );
    }
    
    /**
     * Bid 엔티티 + 닉네임으로 BidResponse 생성
     */
    public static BidResponse from(Bid bid, String bidderNickname) {
        return new BidResponse(
                bid.getId(),
                bid.getAuctionId(),
                bid.getBidderId(),
                bidderNickname,
                bid.getBidAmount(),
                bid.getStatus(),
                bid.getIsWinning(),
                bid.getCreatedAt()
        );
    }
}
