package com.fourtune.auction.boundedContext.watchList.domain;

import com.fourtune.common.shared.watchList.domain.ReplicaAuctionItem;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "watch_list_auction_items")
public class WatchListAuctionItem extends ReplicaAuctionItem {

    @Builder
    public WatchListAuctionItem(Long id, String title, BigDecimal currentPrice,
                                BigDecimal buyNowPrice, String thumbnailImageUrl) {
        super(id, title, currentPrice, buyNowPrice, thumbnailImageUrl);
    }

    public void updateSync(String title, BigDecimal currentPrice, String thumbnailImageUrl) {
        super.updateInfo(title, currentPrice, thumbnailImageUrl);
    }

}
