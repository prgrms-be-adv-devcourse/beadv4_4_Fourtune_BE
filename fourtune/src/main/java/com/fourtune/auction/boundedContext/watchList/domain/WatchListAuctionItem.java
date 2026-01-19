package com.fourtune.auction.boundedContext.watchList.domain;

import com.fourtune.auction.shared.watchList.domain.ReplicaAuctionItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "watch_list_auction_items")
public class WatchListAuctionItem extends ReplicaAuctionItem {

    @Id
    @Column(name = "auction_item_id")
    private Long id;

    private String itemName;
    private BigDecimal currentPrice;

    @Builder
    public WatchListAuctionItem(Long id, String title, BigDecimal currentPrice,
                                BigDecimal buyNowPrice, String thumbnailImageUrl) {
        super(id, title, currentPrice, buyNowPrice, thumbnailImageUrl);
    }

}
