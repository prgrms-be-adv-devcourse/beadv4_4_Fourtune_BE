package com.fourtune.auction.shared.user.event;

import com.fourtune.auction.shared.user.dto.UserResponse;

public record UserSignedUpEvent(
        UserResponse userResponse
) {}
