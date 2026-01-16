package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "item_images")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemImage extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "auction_item_id", nullable = false)
    private AuctionItem auctionItem;
    
    @Column(nullable = false, length = 500)
    private String imageUrl;
    
    @Column(nullable = false)
    private Integer displayOrder;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isThumbnail = false;
}
