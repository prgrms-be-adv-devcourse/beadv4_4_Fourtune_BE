package com.fourtune.auction.shared.user.event;

import com.fourtune.auction.shared.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDeletedEvent {
    private final UserResponse user;
}
