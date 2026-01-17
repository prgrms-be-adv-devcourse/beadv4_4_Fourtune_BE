package com.fourtune.auction.boundedContext.search.application.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SearchProperties {
    // TODO : @Value로 변경 쉽게 페이지 사이즈 설정. 값은 yml에 추가하기
    private int pageSize = 20;
}
