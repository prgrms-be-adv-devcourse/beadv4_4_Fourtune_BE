package com.fourtune.auction.shared.watchList.domain;

import com.fourtune.auction.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplicaAuctionItem extends BaseEntity {

    @Id
    @Column(name = "auction_item_id")
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String title;

    @Column(precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal buyNowPrice;

    private String thumbnailImageUrl;

    public ReplicaAuctionItem(Long id, String title, BigDecimal currentPrice,
                              BigDecimal buyNowPrice,
                              String thumbnailImageUrl) {
        this.id = id;
        this.title = title;
        this.currentPrice = currentPrice;
        this.buyNowPrice = buyNowPrice;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public void updateInfo(String title, BigDecimal currentPrice, String thumbnailImageUrl){
        this.title = title;
        this.currentPrice = currentPrice;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

}
