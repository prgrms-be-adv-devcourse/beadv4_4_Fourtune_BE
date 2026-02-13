package com.fourtune.common.shared.user.event;

import com.fourtune.common.shared.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDeletedEvent {
    private final UserResponse user;
}
