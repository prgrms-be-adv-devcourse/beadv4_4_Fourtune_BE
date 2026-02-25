package com.fourtune.auction.boundedContext.auction.adapter.in.web.dto;

/**
 * 탈퇴 등에서 사용: 해당 유저(판매자)의 진행 중(ACTIVE) 경매 존재 여부·개수.
 */
public record ActiveAuctionsResponse(
        boolean hasActiveAuctions,
        long count
) {
    public static ActiveAuctionsResponse of(long count) {
        return new ActiveAuctionsResponse(count > 0, count);
    }
}
