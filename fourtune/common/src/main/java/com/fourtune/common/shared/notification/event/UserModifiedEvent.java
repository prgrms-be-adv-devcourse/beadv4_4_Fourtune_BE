package com.fourtune.common.shared.notification.event;

import com.fourtune.common.shared.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserModifiedEvent {

    private final UserResponse userResponse;

}
