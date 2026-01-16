package com.fourtune.auction.boundedContext.notification.domain;

import com.fourtune.auction.boundedContext.notification.constant.NotificationType;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sendAt;

    private LocalDateTime readAt;

    @Builder
    public Notification(User user, NotificationType type, String title, String content) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.isRead = false;
        this.sendAt = (sendAt != null) ? sendAt : LocalDateTime.now();
    }

    public void read() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

}
