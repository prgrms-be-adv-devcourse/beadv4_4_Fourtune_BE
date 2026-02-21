package com.fourtune.shared.watchList.dto;

import jakarta.validation.constraints.NotNull;

public record WatchListRequestDto(
        @NotNull(message = "경매 물품 ID는 필수입니다.")
        Long auctionItemId
) {
}
