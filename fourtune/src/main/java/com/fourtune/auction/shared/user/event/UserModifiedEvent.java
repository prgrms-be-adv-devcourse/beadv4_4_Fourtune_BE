package com.fourtune.auction.shared.user.event;

import com.fourtune.auction.shared.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserModifiedEvent {
    private final UserResponse user;
}
