package com.fourtune.auction.boundedContext.search.application.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SearchProperties {
    // TODO : @Value로 변경 쉽게 페이지 사이즈 설정 (yml에 값 추가)

    /**
     * 기본 검색 페이지 사이즈
     */
    // @Value("${search.page.size:20}")
    private int pageSize = 20;

    /**
     * ES index.max_result_window 기준 될 예정
     * 이 값을 넘는 페이징은 search_after 전환 대상일 예정
     */
    // @Value("${search.max.from:10000}")
    private int maxFrom = 10_000;
}
