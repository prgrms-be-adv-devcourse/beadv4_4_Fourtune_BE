package com.fourtune.shared.user.event;

import com.fourtune.shared.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinedEvent {
    private final UserResponse user;
}
