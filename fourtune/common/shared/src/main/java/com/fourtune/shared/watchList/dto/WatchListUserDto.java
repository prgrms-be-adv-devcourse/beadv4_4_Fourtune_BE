package com.fourtune.shared.watchList.dto;

import java.time.LocalDateTime;

public record WatchListUserDto (
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String nickname
){}
