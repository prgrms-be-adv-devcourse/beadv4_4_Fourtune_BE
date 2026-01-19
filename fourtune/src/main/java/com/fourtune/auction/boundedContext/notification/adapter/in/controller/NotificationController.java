package com.fourtune.auction.boundedContext.notification.adapter.in.controller;

import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.shared.auth.dto.UserContext;
import com.fourtune.auction.shared.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationFacade notificationFacade;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal UserContext userContext
            ) {
        log.info("GET /api/v1/notifications - User: {}", userContext.id());

        List<NotificationResponseDto> response = notificationFacade.getMyNotifications(userContext.id());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserContext userContext,
            @PathVariable Long notificationId
    ) {
        log.info("PATCH /api/v1/notifications/{}/read - User: {}", notificationId, userContext.id());

        notificationFacade.markAsRead(userContext.id(), notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal UserContext userContext,
            @RequestBody Long notificationId
    ) {
        log.info("DELETE /api/v1/notifications/{} - User: {}", notificationId, userContext.id());

        notificationFacade.deleteNotification(userContext.id(), notificationId);
        return ResponseEntity.noContent().build();
    }

}
