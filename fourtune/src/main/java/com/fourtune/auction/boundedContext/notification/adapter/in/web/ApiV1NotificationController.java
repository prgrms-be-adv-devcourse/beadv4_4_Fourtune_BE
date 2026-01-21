package com.fourtune.auction.boundedContext.notification.adapter.in.web;

import com.fourtune.auction.boundedContext.notification.application.service.NotificationFacade;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.shared.notification.dto.NotificationResponse;
import com.fourtune.auction.shared.notification.dto.UnreadCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 알림 REST API Controller
 */
@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class ApiV1NotificationController {

    private final NotificationFacade notificationFacade;

    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<NotificationResponse> notifications = notificationFacade.getNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications, "알림 목록 조회 성공"));
    }

    @Operation(summary = "읽지 않은 알림 목록 조회", description = "읽지 않은 알림만 조회합니다.")
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<NotificationResponse> notifications = notificationFacade.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications, "읽지 않은 알림 조회 성공"));
    }

    @Operation(summary = "읽지 않은 알림 개수 조회", description = "읽지 않은 알림 개수를 조회합니다.")
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UnreadCountResponse response = notificationFacade.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "읽지 않은 알림 개수 조회 성공"));
    }

    @Operation(summary = "알림 상세 조회", description = "특정 알림을 상세 조회합니다.")
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        NotificationResponse notification = notificationFacade.getNotification(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success(notification, "알림 조회 성공"));
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationFacade.markAsRead(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "알림 읽음 처리 성공"));
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "모든 알림을 읽음 처리합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        int count = notificationFacade.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(count, count + "개 알림 읽음 처리 성공"));
    }

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationFacade.deleteNotification(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "알림 삭제 성공"));
    }

    @Operation(summary = "읽은 알림 전체 삭제", description = "읽은 알림을 모두 삭제합니다.")
    @DeleteMapping("/read")
    public ResponseEntity<ApiResponse<Void>> deleteReadNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationFacade.deleteReadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "읽은 알림 삭제 성공"));
    }

}
