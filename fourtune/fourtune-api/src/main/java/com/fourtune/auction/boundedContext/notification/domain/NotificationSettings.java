package com.fourtune.auction.boundedContext.notification.domain;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Table(name = "notification_settings")
public class NotificationSettings extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private NotificationUser user;

    @Builder.Default
    private boolean isBidPushEnabled = true;

    @Builder.Default
    private boolean isPaymentPushEnabled = true;

    @Builder.Default
    private boolean isWatchListPushEnabled = true;

    public NotificationSettings(NotificationUser user) {
        this.user = user;
    }

    public void update(boolean isBidPushEnabled, boolean isPaymentPushEnabled, boolean isWatchListPushEnabled) {
        this.isBidPushEnabled = isBidPushEnabled;
        this.isPaymentPushEnabled = isPaymentPushEnabled;
        this.isWatchListPushEnabled = isWatchListPushEnabled;
    }

}
