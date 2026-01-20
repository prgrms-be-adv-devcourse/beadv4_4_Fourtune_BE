package com.fourtune.auction.shared.watchlist.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 관심상품 추가 요청 DTO
 */
public record WatchlistAddRequest(
        @NotNull(message = "경매 ID는 필수입니다.")
        Long auctionId
) {
}
