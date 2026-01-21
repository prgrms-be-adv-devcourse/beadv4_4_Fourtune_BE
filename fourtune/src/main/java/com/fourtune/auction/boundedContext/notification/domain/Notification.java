package com.fourtune.auction.boundedContext.notification.domain;

import com.fourtune.auction.boundedContext.notification.constant.NotificationType;
import com.fourtune.auction.global.common.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 * - 사용자에게 발송된 알림 정보 관리
 * - 입찰, 낙찰, 결제, 관심상품 알림 등
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_id", columnList = "userId"),
        @Index(name = "idx_notification_is_read", columnList = "isRead"),
        @Index(name = "idx_notification_created_at", columnList = "createdAt")
})
public class Notification extends BaseIdAndTime {

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private boolean isRead = false;

    private LocalDateTime readAt;

    // 관련 엔티티 ID (선택적)
    private Long relatedId;

    @Builder
    private Notification(Long userId, NotificationType type, String title, String content, 
                         boolean isRead, Long relatedId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.isRead = isRead;
        this.relatedId = relatedId;
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 알림 생성
     */
    public static Notification create(Long userId, NotificationType type, String title, String content) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .isRead(false)
                .build();
    }

    /**
     * 관련 ID 포함 알림 생성
     */
    public static Notification createWithRelatedId(Long userId, NotificationType type, 
                                                    String title, String content, Long relatedId) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .isRead(false)
                .relatedId(relatedId)
                .build();
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 알림 읽음 처리
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * 읽음 여부 확인
     */
    public boolean isUnread() {
        return !this.isRead;
    }

}
