package com.fourtune.auction.boundedContext.user.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    ACTIVE("활동중"),
    INACTIVE("정지"),
    SUSPENDED("탈퇴");

    private final String description;
}
