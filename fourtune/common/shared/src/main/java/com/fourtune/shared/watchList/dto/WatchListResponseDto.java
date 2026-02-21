package com.fourtune.shared.watchList.dto;

import java.math.BigDecimal;

public record WatchListResponseDto(
    long userId,
    long itemId,
    String title,
    BigDecimal currentPrice,
    BigDecimal buyNowPrice,
    String thumbnailImageUrl
){

}
