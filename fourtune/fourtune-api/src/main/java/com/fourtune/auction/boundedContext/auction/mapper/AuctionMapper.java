package com.fourtune.auction.boundedContext.auction.mapper;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.common.shared.auction.dto.AuctionItemDetailResponse;
import com.fourtune.common.shared.auction.dto.AuctionItemResponse;
import org.springframework.stereotype.Component;

import java.util.List;

public class AuctionMapper{

    public static AuctionItemDetailResponse fromDetail(
            com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem auctionItem
    ) {
        return fromDetail(auctionItem, null);
    }

    public static AuctionItemDetailResponse fromDetail(AuctionItem auctionItem, String sellerNickname){
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
                auctionItem.getCategory().toString(),
                auctionItem.getStartPrice(),
                auctionItem.getCurrentPrice(),
                auctionItem.getBidUnit(),
                auctionItem.getBuyNowPrice(),
                auctionItem.getBuyNowEnabled(),
                auctionItem.getBuyNowDisabledByPolicy(),
                auctionItem.getStatus().toString(),
                auctionItem.getAuctionStartTime(),
                auctionItem.getAuctionEndTime(),
                auctionItem.getViewCount(),
                auctionItem.getWatchlistCount(),
                auctionItem.getBidCount(),
                imageUrls
        );
    }

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
                auctionItem.getCategory().toString(),
                auctionItem.getStartPrice(),
                auctionItem.getCurrentPrice(),
                auctionItem.getBuyNowPrice(),
                auctionItem.getBuyNowEnabled(),
                auctionItem.getBuyNowDisabledByPolicy(),
                auctionItem.getStatus().toString(),
                auctionItem.getAuctionEndTime(),
                auctionItem.getViewCount(),
                auctionItem.getWatchlistCount(),
                auctionItem.getBidCount(),
                thumbnailUrl
        );
    }

}
