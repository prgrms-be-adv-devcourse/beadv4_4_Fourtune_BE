package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionItemResponse(
    Long id,
    Long sellerId,
    String sellerNickname,
    String title,
    Category category,
    BigDecimal startPrice,
    BigDecimal currentPrice,
    BigDecimal buyNowPrice,
    Boolean buyNowEnabled,
    Boolean buyNowDisabledByPolicy,
    AuctionStatus status,
    LocalDateTime auctionEndTime,
    Long viewCount,
    Integer bidCount,
    String thumbnailUrl
) {
    /**
     * 엔티티만으로 생성 (sellerNickname 없음, 내부/테스트용)
     */
    public static AuctionItemResponse from(com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem) {
        return from(auctionItem, null);
    }

    /**
     * 엔티티 + 판매자 닉네임으로 생성 (API 응답용)
     */
    public static AuctionItemResponse from(
            com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem,
            String sellerNickname
    ) {
        String thumbnailUrl = auctionItem.getImages() != null && !auctionItem.getImages().isEmpty()
                ? auctionItem.getImages().get(0).getImageUrl()
                : null;

        return new AuctionItemResponse(
                auctionItem.getId(),
                auctionItem.getSellerId(),
                sellerNickname,
                auctionItem.getTitle(),
                auctionItem.getCategory(),
                auctionItem.getStartPrice(),
                auctionItem.getCurrentPrice(),
                auctionItem.getBuyNowPrice(),
                auctionItem.getBuyNowEnabled(),
                auctionItem.getBuyNowDisabledByPolicy(),
                auctionItem.getStatus(),
                auctionItem.getAuctionEndTime(),
                auctionItem.getViewCount(),
                auctionItem.getBidCount(),
                thumbnailUrl
        );
    }

    /**
     * 조회수만 바꾼 복사 (Redis 합산 값 반영용)
     */
    public AuctionItemResponse withViewCount(long viewCount) {
        return new AuctionItemResponse(
                id, sellerId, sellerNickname, title, category,
                startPrice, currentPrice, buyNowPrice, buyNowEnabled, buyNowDisabledByPolicy,
                status, auctionEndTime, viewCount, bidCount, thumbnailUrl
        );
    }
}
