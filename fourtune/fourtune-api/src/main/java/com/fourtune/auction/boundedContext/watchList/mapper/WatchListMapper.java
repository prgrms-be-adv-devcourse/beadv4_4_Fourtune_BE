package com.fourtune.auction.boundedContext.watchList.mapper;

import com.fourtune.auction.boundedContext.watchList.domain.WatchList;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListAuctionItem;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListUser;
import com.fourtune.common.shared.watchList.dto.WatchListResponseDto;
import org.springframework.stereotype.Component;

public class WatchListMapper {

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
