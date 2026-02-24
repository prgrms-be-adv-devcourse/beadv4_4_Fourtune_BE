package com.fourtune.auction.boundedContext.user.adapter.out.external.dto;

/**
 * auction-service 내부 API 응답 DTO.
 * auction-service의 ActiveAuctionsResponse와 동일한 구조.
 */
public record ActiveAuctionsResponse(
        boolean hasActiveAuctions,
        long count
) {
}
