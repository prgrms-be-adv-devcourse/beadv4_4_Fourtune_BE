package com.fourtune.shared.user.event;

import com.fourtune.shared.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDeletedEvent {
    private final UserResponse user;
}
