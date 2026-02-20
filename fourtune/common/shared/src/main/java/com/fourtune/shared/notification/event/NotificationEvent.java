package com.fourtune.shared.notification.event;

public record NotificationEvent (
        Long receiverId,
        String title,
        String content,
        String relatedUrl
){
}
