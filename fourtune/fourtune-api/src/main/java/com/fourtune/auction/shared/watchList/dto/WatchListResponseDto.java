package com.fourtune.auction.shared.watchList.dto;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.watchList.domain.WatchList;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListAuctionItem;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListUser;

import java.math.BigDecimal;

public record WatchListResponseDto(
    long userId,
    long itemId,
    String title,
    BigDecimal currentPrice,
    BigDecimal buyNowPrice,
    String thumbnailImageUrl
){

    public static WatchListResponseDto from(WatchList watchList){
        WatchListAuctionItem item = watchList.getAuctionItem();
        WatchListUser user = watchList.getUser();

        return new WatchListResponseDto(
                user.getId(),
                item.getId(),
                item.getTitle(),
                item.getCurrentPrice(),
                item.getBuyNowPrice(),
                item.getThumbnailImageUrl()
        );
    }

}
