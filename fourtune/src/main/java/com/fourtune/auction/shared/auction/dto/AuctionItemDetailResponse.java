package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionItemDetailResponse(
    Long id,
    Long sellerId,
    String title,
    String description,
    Category category,
    BigDecimal startPrice,
    BigDecimal currentPrice,
    Integer bidUnit,
    BigDecimal buyNowPrice,
    Boolean buyNowEnabled,
    Boolean buyNowDisabledByPolicy,
    AuctionStatus status,
    LocalDateTime auctionStartTime,
    LocalDateTime auctionEndTime,
    Long viewCount,
    Integer watchlistCount,
    Integer bidCount,
    List<String> imageUrls
) {
    /**
     * AuctionItem 엔티티로부터 상세 응답 DTO 생성
     */
    public static AuctionItemDetailResponse from(
            com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem
    ) {
        List<String> imageUrls = auctionItem.getImages() != null 
                ? auctionItem.getImages().stream()
                    .map(com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage::getImageUrl)
                    .toList()
                : List.of();
        
        return new AuctionItemDetailResponse(
                auctionItem.getId(),
                auctionItem.getSellerId(),
                auctionItem.getTitle(),
                auctionItem.getDescription(),
                auctionItem.getCategory(),
                auctionItem.getStartPrice(),
                auctionItem.getCurrentPrice(),
                auctionItem.getBidUnit(),
                auctionItem.getBuyNowPrice(),
                auctionItem.getBuyNowEnabled(),
                auctionItem.getBuyNowDisabledByPolicy(),
                auctionItem.getStatus(),
                auctionItem.getAuctionStartTime(),
                auctionItem.getAuctionEndTime(),
                auctionItem.getViewCount(),
                auctionItem.getWatchlistCount(),
                auctionItem.getBidCount(),
                imageUrls
        );
    }
}
