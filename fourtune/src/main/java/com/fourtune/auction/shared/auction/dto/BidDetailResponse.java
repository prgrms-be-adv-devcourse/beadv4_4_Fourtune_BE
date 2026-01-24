package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;

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
    /**
     * Bid와 AuctionItem으로 BidDetailResponse 생성
     */
    public static BidDetailResponse from(Bid bid, AuctionItem auctionItem) {
        return new BidDetailResponse(
                bid.getId(),
                bid.getAuctionId(),
                auctionItem.getTitle(),
                bid.getBidderId(),
                null, // bidderNickname은 User 조회 필요
                bid.getBidAmount(),
                bid.getStatus(),
                bid.getIsWinning(),
                bid.getCreatedAt(),
                generateMessage(bid)
        );
    }
    
    /**
     * Bid, AuctionItem, 닉네임으로 BidDetailResponse 생성
     */
    public static BidDetailResponse from(Bid bid, AuctionItem auctionItem, String bidderNickname) {
        return new BidDetailResponse(
                bid.getId(),
                bid.getAuctionId(),
                auctionItem.getTitle(),
                bid.getBidderId(),
                bidderNickname,
                bid.getBidAmount(),
                bid.getStatus(),
                bid.getIsWinning(),
                bid.getCreatedAt(),
                generateMessage(bid)
        );
    }
    
    /**
     * 입찰 상태에 따른 메시지 생성
     */
    private static String generateMessage(Bid bid) {
        return switch (bid.getStatus()) {
            case ACTIVE -> bid.getIsWinning() ? "현재 최고 입찰자입니다." : "입찰이 등록되었습니다.";
            case SUCCESS -> "축하합니다! 낙찰되었습니다.";
            case FAILED -> "아쉽게도 낙찰되지 않았습니다.";
            case CANCELLED -> "입찰이 취소되었습니다.";
        };
    }
}
