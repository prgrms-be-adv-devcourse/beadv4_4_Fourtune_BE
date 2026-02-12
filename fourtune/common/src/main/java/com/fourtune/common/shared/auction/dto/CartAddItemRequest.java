package com.fourtune.common.shared.auction.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 장바구니 아이템 추가 요청 DTO
 */
public record CartAddItemRequest(
    @NotNull Long auctionId
) {
    // TODO: 검증 로직
    // - 즉시구매 가능 여부 확인
    // - 이미 장바구니에 있는지 확인
}
