package com.fourtune.common.shared.user.event;

import com.fourtune.common.shared.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinedEvent {
    private final UserResponse user;
}
