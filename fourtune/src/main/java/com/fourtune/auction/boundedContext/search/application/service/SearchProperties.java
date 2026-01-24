package com.fourtune.auction.boundedContext.search.application.service;

import lombok.Getter;
import lombok.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SearchProperties {

    private final int pageSize = 20;
    private final int maxFrom = 10000;

    // TODO : @Value로 변경 쉽게 페이지 사이즈 설정 (yml에 값 추가)
//    @Value("${search.page.size:20}")
//    private int pageSize = 20;
//
//    @Value("${search.max.from:10000}")
//    private int maxFrom = 10_000;
}
