package com.fourtune.shared.auction.dto;

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
    String auctionStatus,
    String status,
    LocalDateTime addedAt,
    Boolean isPriceChanged // 가격 변동 여부
) {

}
