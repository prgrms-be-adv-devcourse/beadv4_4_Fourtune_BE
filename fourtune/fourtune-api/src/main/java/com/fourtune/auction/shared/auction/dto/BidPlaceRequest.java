package com.fourtune.auction.shared.auction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 입찰 등록 요청 DTO
 */
public record BidPlaceRequest(
    @NotNull Long auctionId,
    @NotNull @Min(1000) BigDecimal bidAmount
) {
    // TODO: 검증 로직 추가
    // - 입찰 금액이 입찰 단위에 맞는지
    // - 최소 입찰가 이상인지
}
