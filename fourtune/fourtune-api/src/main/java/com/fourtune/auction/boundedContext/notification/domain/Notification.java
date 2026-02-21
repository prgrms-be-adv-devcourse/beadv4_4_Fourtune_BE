package com.fourtune.auction.boundedContext.notification.domain;

import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private NotificationUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column
    private String relatedUrl;

    @Column(nullable = false)
    private boolean isRead;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime sendAt;

    private LocalDateTime readAt;

    @Builder
    public Notification(NotificationUser user, NotificationType type, String title, String content, String relatedUrl) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.relatedUrl = relatedUrl;
        this.isRead = false;
    }

    public void read() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long userId){
        if(userId == null || this.user == null)
            return false;

        return this.user.getId().equals(userId);
    }

}
