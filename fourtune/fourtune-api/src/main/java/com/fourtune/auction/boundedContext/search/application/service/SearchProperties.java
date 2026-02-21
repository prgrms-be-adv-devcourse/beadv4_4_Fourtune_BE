package com.fourtune.auction.boundedContext.search.application.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 검색 관련 설정 프로퍼티
 * - application.yml의 search.* 설정을 바인딩
 */
@Getter
@Component
public class SearchProperties {

    // 검색 결과 페이지 크기 (기본: 20)
    @Value("${search.page-size:20}")
    private int pageSize;

    // Deep paging 최대 from 값 (기본: 10000)
    @Value("${search.max-from:10000}")
    private int maxFrom;

    // 검색 키워드 최대 길이 (기본: 100)
    @Value("${search.max-keyword-length:100}")
    private int maxKeywordLength;
}
