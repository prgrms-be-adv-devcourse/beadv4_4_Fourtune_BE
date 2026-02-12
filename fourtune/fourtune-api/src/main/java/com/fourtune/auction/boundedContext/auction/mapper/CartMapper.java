package com.fourtune.auction.boundedContext.auction.mapper;

import com.fourtune.auction.boundedContext.auction.domain.constant.CartItemStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import com.fourtune.auction.boundedContext.auction.domain.entity.CartItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.common.shared.auction.dto.CartItemResponse;
import com.fourtune.common.shared.auction.dto.CartResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

public class CartMapper {
    /**
     * CartItem과 AuctionItem으로 CartItemResponse 생성
     */
    public static CartItemResponse from(CartItem cartItem, AuctionItem auctionItem) {
        String thumbnailUrl = null;
        BigDecimal currentBuyNowPrice = null;
        String auctionStatus = null;

        if (auctionItem != null) {
            thumbnailUrl = auctionItem.getImages().stream()
                    .filter(ItemImage::getIsThumbnail)
                    .findFirst()
                    .map(ItemImage::getImageUrl)
                    .orElse(null);
            currentBuyNowPrice = auctionItem.getBuyNowPrice();
            auctionStatus = auctionItem.getStatus().toString();
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
                cartItem.getStatus().toString(),
                cartItem.getCreatedAt(),
                isPriceChanged
        );
    }
    /**
     * Cart와 CartItemResponse 목록으로 CartResponse 생성
     */
    public static CartResponse from(Cart cart, List<CartItemResponse> items) {
        int activeCount = (int) items.stream()
                .filter(item -> item.status().equals(CartItemStatus.ACTIVE.toString()))
                .count();

        BigDecimal totalPrice = items.stream()
                .filter(item -> item.status().equals(CartItemStatus.ACTIVE.toString()))
                .map(item -> item.currentBuyNowPrice() != null
                        ? item.currentBuyNowPrice()
                        : item.buyNowPriceWhenAdded())
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items.size(),
                activeCount,
                totalPrice,
                items
        );
    }
}
