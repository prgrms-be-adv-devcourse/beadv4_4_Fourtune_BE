package com.fourtune.shared.auction.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 장바구니에서 즉시구매 요청 DTO
 */
public record CartBuyNowRequest(
    @NotEmpty List<Long> cartItemIds
) {
    // TODO: 검증 로직
    // - 모든 아이템이 활성 상태인지
    // - 즉시구매 가능한 상태인지
}
