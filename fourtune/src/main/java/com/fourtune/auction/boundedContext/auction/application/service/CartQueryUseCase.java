package com.fourtune.auction.boundedContext.auction.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 장바구니 조회 UseCase
 * - 장바구니 내역 조회
 * - 활성 아이템만 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartQueryUseCase {

    private final CartSupport cartSupport;
    private final AuctionSupport auctionSupport;

    /**
     * 사용자의 장바구니 조회
     */
    public Object getUserCart(Long userId) {
        // TODO: 구현 필요
        // 1. 장바구니 조회 (없으면 빈 장바구니 반환)
        // 2. 활성 아이템 목록 조회
        // 3. 각 아이템의 경매 정보 조회 (제목, 이미지, 현재 상태)
        // 4. DTO 변환 후 반환
        return null;
    }

    /**
     * 장바구니 활성 아이템 개수 조회
     */
    public int getActiveItemCount(Long userId) {
        // TODO: 구현 필요
        // 1. 장바구니 조회
        // 2. 활성 아이템 개수 반환
        return 0;
    }

    /**
     * 장바구니 총액 계산
     */
    public Object calculateTotalPrice(Long userId) {
        // TODO: 구현 필요
        // 1. 장바구니 조회
        // 2. 활성 아이템들의 즉시구매가 합계 계산
        // 3. DTO 변환 후 반환
        return null;
    }

}
