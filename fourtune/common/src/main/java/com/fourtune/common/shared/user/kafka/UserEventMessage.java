package com.fourtune.common.shared.user.kafka;

import com.fourtune.common.shared.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka를 통해 전송되는 User 이벤트 메시지
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventMessage {

    private String messageId;
    private String eventType;
    private Long userId;
    private String email;
    private String nickname;
    private String status;
    private String role;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
    private LocalDateTime eventTimestamp;

    public UserResponse toUserResponse() {
        return new UserResponse(
                this.userId,
                this.userCreatedAt,
                this.userUpdatedAt,
                this.email,
                this.nickname,
                this.status,
                this.role
        );
    }
}
