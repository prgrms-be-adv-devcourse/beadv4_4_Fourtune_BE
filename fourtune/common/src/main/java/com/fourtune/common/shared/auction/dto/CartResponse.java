package com.fourtune.common.shared.auction.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 장바구니 응답 DTO
 */
public record CartResponse(
    Long id,
    Long userId,
    Integer totalItemCount,
    Integer activeItemCount,
    BigDecimal totalPrice, // 활성 아이템들의 즉시구매가 합계
    List<CartItemResponse> items
) {
}
