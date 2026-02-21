package com.fourtune.shared.user.event;

import com.fourtune.shared.user.dto.UserResponse;

public record UserSignedUpEvent(
        UserResponse userResponse
) {}
