package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.CartItemStatus;

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
    // TODO: from(CartItem, AuctionItem) 정적 팩토리 메서드 구현
}
