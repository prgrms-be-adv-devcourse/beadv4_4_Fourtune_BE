package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.CartItemStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.CartItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 장바구니 아이템 응답 DTO
 */
public record CartItemResponse(
    Long id,
    Long auctionId,
    String auctionTitle,
    String thumbnailUrl,
    BigDecimal buyNowPriceWhenAdded,
    BigDecimal currentBuyNowPrice,
    AuctionStatus auctionStatus,
    CartItemStatus status,
    LocalDateTime addedAt,
    Boolean isPriceChanged // 가격 변동 여부
) {
    /**
     * CartItem과 AuctionItem으로 CartItemResponse 생성
     */
    public static CartItemResponse from(CartItem cartItem, AuctionItem auctionItem) {
        String thumbnailUrl = null;
        BigDecimal currentBuyNowPrice = null;
        AuctionStatus auctionStatus = null;
        
        if (auctionItem != null) {
            thumbnailUrl = auctionItem.getImages().stream()
                    .filter(ItemImage::getIsThumbnail)
                    .findFirst()
                    .map(ItemImage::getImageUrl)
                    .orElse(null);
            currentBuyNowPrice = auctionItem.getBuyNowPrice();
            auctionStatus = auctionItem.getStatus();
        }
        
        // 가격 변동 여부 확인
        boolean isPriceChanged = currentBuyNowPrice != null 
                && cartItem.getBuyNowPriceWhenAdded() != null
                && currentBuyNowPrice.compareTo(cartItem.getBuyNowPriceWhenAdded()) != 0;
        
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getAuctionId(),
                auctionItem != null ? auctionItem.getTitle() : null,
                thumbnailUrl,
                cartItem.getBuyNowPriceWhenAdded(),
                currentBuyNowPrice,
                auctionStatus,
                cartItem.getStatus(),
                cartItem.getCreatedAt(),
                isPriceChanged
        );
    }
}
