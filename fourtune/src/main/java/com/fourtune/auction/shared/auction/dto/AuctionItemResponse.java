package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionItemResponse(
    Long id,
    Long sellerId,
    String title,
    Category category,
    BigDecimal startPrice,
    BigDecimal currentPrice,
    BigDecimal buyNowPrice,
    Boolean buyNowEnabled,
    AuctionStatus status,
    LocalDateTime auctionEndTime,
    Long viewCount,
    Integer bidCount,
    String thumbnailUrl
) {
    public static AuctionItemResponse from(com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem) {
        String thumbnailUrl = auctionItem.getImages() != null && !auctionItem.getImages().isEmpty()
                ? auctionItem.getImages().get(0).getImageUrl()
                : null;
        
        return new AuctionItemResponse(
                auctionItem.getId(),
                auctionItem.getSellerId(),
                auctionItem.getTitle(),
                auctionItem.getCategory(),
                auctionItem.getStartPrice(),
                auctionItem.getCurrentPrice(),
                auctionItem.getBuyNowPrice(),
                auctionItem.getBuyNowEnabled(),
                auctionItem.getStatus(),
                auctionItem.getAuctionEndTime(),
                auctionItem.getViewCount(),
                auctionItem.getBidCount(),
                thumbnailUrl
        );
    }
}
