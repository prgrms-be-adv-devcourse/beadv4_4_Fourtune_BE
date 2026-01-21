package com.fourtune.auction.boundedContext.notification.domain;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_settings")
public class NotificationSettings extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private boolean isBidPushEnabled;
    private boolean isPaymentPushEnabled;
    private boolean isWatchListPushEnabled;

    @Builder
    public NotificationSettings(User user) {
        this.user = user;
        this.isBidPushEnabled = true;
        this.isPaymentPushEnabled = true;
        this.isWatchListPushEnabled = true;
    }

    public void update(boolean isBidPushEnabled, boolean isPaymentPushEnabled, boolean isWatchListPushEnabled) {
        this.isBidPushEnabled = isBidPushEnabled;
        this.isPaymentPushEnabled = isPaymentPushEnabled;
        this.isWatchListPushEnabled = isWatchListPushEnabled;
    }

}
