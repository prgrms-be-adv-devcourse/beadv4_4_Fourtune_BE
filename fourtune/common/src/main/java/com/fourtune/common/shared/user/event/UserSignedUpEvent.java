package com.fourtune.common.shared.user.event;

import com.fourtune.common.shared.user.dto.UserResponse;

public record UserSignedUpEvent(
        UserResponse userResponse
) {}
