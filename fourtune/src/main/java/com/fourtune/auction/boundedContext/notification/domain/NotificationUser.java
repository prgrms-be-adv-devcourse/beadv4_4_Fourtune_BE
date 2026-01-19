package com.fourtune.auction.boundedContext.notification.domain;

import com.fourtune.auction.shared.user.domain.ReplicaUser;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_users")
@Getter
@NoArgsConstructor
public class NotificationUser extends ReplicaUser {

    @Builder
    public NotificationUser(
            Long id,
            String email,
            String nickname,
            String password,
            String phoneNumber,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        super(id, email, nickname, password, phoneNumber, createdAt, updatedAt, deletedAt);
    }

    public void syncProfile(String nickname, String email) {
        super.updateInfo(nickname, email);
    }

}
