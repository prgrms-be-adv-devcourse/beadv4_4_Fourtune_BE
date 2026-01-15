package com.fourtune.auction.boundedContext.auction.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    ELECTRONICS("전자기기"),
    CLOTHING("의류"),
    POTTERY("도자기"),
    APPLIANCES("가전제품"),
    BEDDING("침구"),
    BOOKS("도서"),
    COLLECTIBLES("수집품"),
    ETC("기타");

    private final String description;
}
