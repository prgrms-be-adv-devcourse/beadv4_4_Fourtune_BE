package com.fourtune.auction.boundedContext.watchList.domain;

import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchList extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private WatchListUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_item_id")
    private WatchListAuctionItem auctionItem;

    private boolean isStartAlertSent;
    private boolean isEndAlertSent;

    @Builder
    public WatchList(WatchListUser user, WatchListAuctionItem auctionItem) {
        this.user = user;
        this.auctionItem = auctionItem;
        this.isStartAlertSent = false;
        this.isEndAlertSent = false;
    }

    public void markStartAlertSent() {
        this.isStartAlertSent = true;
    }

    public void markEndAlertSent() {
        this.isEndAlertSent = true;
    }

}
