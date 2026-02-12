package com.fourtune.common.shared.notification.event;

public record NotificationEvent (
        Long receiverId,
        String title,
        String content,
        String relatedUrl
){
}
