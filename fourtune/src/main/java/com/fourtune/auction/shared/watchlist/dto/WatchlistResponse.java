package com.fourtune.auction.shared.watchlist.dto;

import com.fourtune.auction.boundedContext.watchlist.domain.entity.Watchlist;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 관심상품 응답 DTO
 */
public record WatchlistResponse(
        long id,
        Long auctionId,
        String auctionTitle,
        BigDecimal priceAtAdded,
        LocalDateTime auctionEndTime,
        boolean notifyOnEndingSoon,
        boolean notifyOnPriceChange,
        LocalDateTime createdAt
) {
    public static WatchlistResponse from(Watchlist watchlist) {
        return new WatchlistResponse(
                watchlist.getId(),
                watchlist.getAuctionId(),
                watchlist.getAuctionTitle(),
                watchlist.getPriceAtAdded(),
                watchlist.getAuctionEndTime(),
                watchlist.isNotifyOnEndingSoon(),
                watchlist.isNotifyOnPriceChange(),
                watchlist.getCreatedAt()
        );
    }
}
