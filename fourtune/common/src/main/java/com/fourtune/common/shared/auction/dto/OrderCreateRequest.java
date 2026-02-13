package com.fourtune.common.shared.auction.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 주문 생성 요청 DTO
 * (낙찰 또는 즉시구매 시 자동 생성되므로 외부에서 직접 호출하지 않음)
 */
public record OrderCreateRequest(
    @NotNull Long auctionId,
    @NotNull Long winnerId,
    @NotNull BigDecimal finalPrice,
    @NotNull String orderType // "AUCTION_WIN" or "BUY_NOW"
) {
    // TODO: 내부 사용 전용 DTO
    // 외부 API에서는 사용하지 않고, UseCase 간 데이터 전달용
}
