package com.fourtune.auction.boundedContext.auction.mapper;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.shared.auction.dto.BidDetailResponse;
import com.fourtune.shared.auction.dto.BidHistoryResponse;
import com.fourtune.shared.auction.dto.BidResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

public class BidMapper {

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
                bid.getStatus().toString(),
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
                bid.getStatus().toString(),
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

    /**
     * AuctionItem과 Bid 목록으로 BidHistoryResponse 생성
     */
    public static BidHistoryResponse from(AuctionItem auctionItem, List<Bid> bids) {
        return from(auctionItem, bids, null);
    }

    /**
     * AuctionItem, Bid 목록, bidderId별 닉네임 맵으로 BidHistoryResponse 생성
     */
    public static BidHistoryResponse from(AuctionItem auctionItem, List<Bid> bids,
                                          Map<Long, String> bidderNicknames) {
        List<BidResponse> bidResponses = bids.stream()
                .map(bid -> BidMapper.from(bid,
                        bidderNicknames != null ? bidderNicknames.get(bid.getBidderId()) : null))
                .toList();
        return new BidHistoryResponse(
                auctionItem.getId(),
                auctionItem.getTitle(),
                auctionItem.getBidCount(),
                bidResponses
        );
    }

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
                bid.getStatus().toString(),
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
                bid.getStatus().toString(),
                bid.getIsWinning(),
                bid.getCreatedAt()
        );
    }

}
