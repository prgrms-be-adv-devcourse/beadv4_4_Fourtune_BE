package com.fourtune.auction.boundedContext.watchList.domain;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "watch_list")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchList extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_item_id", nullable = false)
    private AuctionItem auctionItem;

    private boolean isStartAlertSent;
    private boolean isEndAlertSent;

    @Builder
    public WatchList(User user, AuctionItem auctionItem) {
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
