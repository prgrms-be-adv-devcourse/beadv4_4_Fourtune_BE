package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "auction_items")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionItem extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long sellerId;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal startPrice;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer bidUnit = 1000;
    
    // 즉시구매 관련 필드
    @Column(precision = 19, scale = 2)
    private BigDecimal buyNowPrice;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean buyNowEnabled = false;
    
    @Column(nullable = false)
    private LocalDateTime auctionStartTime;
    
    @Column(nullable = false)
    private LocalDateTime auctionEndTime;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionStatus status = AuctionStatus.SCHEDULED;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal currentPrice;
    
    @Builder.Default
    private Long viewCount = 0L;
    
    @Builder.Default
    private Integer watchlistCount = 0;
    
    @Builder.Default
    private Integer bidCount = 0;
    
    @OneToMany(mappedBy = "auctionItem", cascade = ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemImage> images = new ArrayList<>();
    
    // TODO: 비즈니스 메서드 구현
    // - create()
    // - update()
    // - start()
    // - close()
    // - sell()
    // - cancel()
    // - extend()
    // - increaseViewCount()
    // - increaseBidCount()
    // - updateCurrentPrice()
}
