package com.fourtune.auction.shared.notification.dto;

public record NotificationEvent (
        Long receiverId,
        String title,
        String content,
        String relatedUrl
){
}
