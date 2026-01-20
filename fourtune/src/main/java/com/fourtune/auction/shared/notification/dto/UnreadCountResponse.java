package com.fourtune.auction.shared.notification.dto;

/**
 * 읽지 않은 알림 개수 응답 DTO
 */
public record UnreadCountResponse(
        long count
) {
}
