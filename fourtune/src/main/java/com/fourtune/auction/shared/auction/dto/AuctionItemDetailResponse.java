package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionItemDetailResponse(
    Long id,
    Long sellerId,
    String sellerNickname,
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
     * AuctionItem 엔티티만으로 생성 (sellerNickname 없음, 내부/테스트용)
     */
    public static AuctionItemDetailResponse from(
            com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem
    ) {
        return from(auctionItem, null);
    }

    /**
     * AuctionItem 엔티티 + 판매자 닉네임으로 상세 응답 DTO 생성 (API 응답용)
     */
    public static AuctionItemDetailResponse from(
            com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem,
            String sellerNickname
    ) {
        List<String> imageUrls = auctionItem.getImages() != null
                ? auctionItem.getImages().stream()
                    .map(com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage::getImageUrl)
                    .toList()
                : List.of();

        return new AuctionItemDetailResponse(
                auctionItem.getId(),
                auctionItem.getSellerId(),
                sellerNickname,
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

    /**
     * 조회수만 바꾼 복사 (Redis 합산 값 반영용)
     */
    public AuctionItemDetailResponse withViewCount(long viewCount) {
        return new AuctionItemDetailResponse(
                id, sellerId, sellerNickname, title, description, category,
                startPrice, currentPrice, bidUnit, buyNowPrice, buyNowEnabled, buyNowDisabledByPolicy,
                status, auctionStartTime, auctionEndTime, viewCount, watchlistCount, bidCount, imageUrls
        );
    }
}
