package com.fourtune.auction.shared.user.kafka;

import com.fourtune.auction.boundedContext.user.domain.constant.UserEventType;
import com.fourtune.auction.shared.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka를 통해 전송되는 User 이벤트 메시지
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventMessage {

    private String messageId;
    private UserEventType eventType;
    private Long userId;
    private String email;
    private String nickname;
    private String status;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
    private LocalDateTime eventTimestamp;

    public static UserEventMessage fromUserJoined(UserResponse user) {
        return UserEventMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType(UserEventType.USER_JOINED)
                .userId(user.id())
                .email(user.email())
                .nickname(user.nickname())
                .status(user.status())
                .userCreatedAt(user.createdAt())
                .userUpdatedAt(user.updatedAt())
                .eventTimestamp(LocalDateTime.now())
                .build();
    }

    public static UserEventMessage fromUserModified(UserResponse user) {
        return UserEventMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType(UserEventType.USER_MODIFIED)
                .userId(user.id())
                .email(user.email())
                .nickname(user.nickname())
                .status(user.status())
                .userCreatedAt(user.createdAt())
                .userUpdatedAt(user.updatedAt())
                .eventTimestamp(LocalDateTime.now())
                .build();
    }

    public static UserEventMessage fromUserDeleted(UserResponse user) {
        return UserEventMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType(UserEventType.USER_DELETED)
                .userId(user.id())
                .email(user.email())
                .nickname(user.nickname())
                .status(user.status())
                .userCreatedAt(user.createdAt())
                .userUpdatedAt(user.updatedAt())
                .eventTimestamp(LocalDateTime.now())
                .build();
    }

    public UserResponse toUserResponse() {
        return new UserResponse(
                this.userId,
                this.userCreatedAt,
                this.userUpdatedAt,
                this.email,
                this.nickname,
                this.status
        );
    }
}
