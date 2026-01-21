package com.fourtune.auction.boundedContext.auction.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 경매 검색 UseCase
 * - Elasticsearch 검색
 * - 한글 형태소 분석 (Nori)
 * - 카테고리, 가격대, 키워드 검색
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionSearchUseCase {

    // private final AuctionSearchAdapter auctionSearchAdapter; // TODO: Elasticsearch 구현 후 추가

    /**
     * 키워드로 경매 검색
     */
    public Page<Object> searchByKeyword(String keyword, Pageable pageable) {
        // TODO: 구현 필요 (Elasticsearch)
        // 1. Elasticsearch 검색
        // 2. 한글 형태소 분석 (Nori Analyzer)
        // 3. 제목, 설명에서 검색
        // 4. DTO 변환 후 반환
        return null;
    }

    /**
     * 복합 조건 검색
     */
    public Page<Object> searchByCondition(Object searchCondition, Pageable pageable) {
        // TODO: 구현 필요 (Elasticsearch)
        // 1. 카테고리 필터링
        // 2. 가격대 필터링
        // 3. 상태 필터링
        // 4. 키워드 검색
        // 5. 정렬 (가격순, 최신순, 인기순)
        return null;
    }

    /**
     * 인기 경매 조회 (조회수 기준)
     */
    public Page<Object> getPopularAuctions(Pageable pageable) {
        // TODO: 구현 필요 (Elasticsearch or Redis)
        // 1. 조회수 기준 정렬
        // 2. DTO 변환 후 반환
        return null;
    }

}
