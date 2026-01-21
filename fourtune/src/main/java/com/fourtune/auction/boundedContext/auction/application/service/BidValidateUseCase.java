package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 입찰 검증 UseCase
 * - 입찰 가능 여부 검증
 * - 입찰 금액 검증
 * - 입찰 자격 검증
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidValidateUseCase {

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;

    /**
     * 입찰 가능 여부 종합 검증
     */
    public void validateBidPlaceable(Long auctionId, Long bidderId, BigDecimal bidAmount) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 경매 상태 검증 (ACTIVE인지)
        // 3. 판매자 검증 (본인이 판매자가 아닌지)
        // 4. 입찰 금액 검증 (validateBidAmount 호출)
        // 5. 경매 시간 검증 (아직 종료되지 않았는지)
    }

    /**
     * 입찰 금액 검증
     */
    public void validateBidAmount(Long auctionId, BigDecimal bidAmount) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 현재 최고가 조회
        // 3. 최소 입찰가 계산 (현재가 or 시작가 + 입찰단위)
        // 4. 입찰 금액이 최소 입찰가 이상인지 확인
        // 5. 입찰 단위에 맞는지 확인 (bidUnit의 배수인지)
    }

    /**
     * 입찰 자격 검증
     */
    public void validateBidderEligibility(Long auctionId, Long bidderId) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 본인이 판매자가 아닌지 확인
        // 3. 사용자 상태 확인 (활성 상태인지)
        // 4. 캐시 잔액 확인 (선택사항 - 입찰 보증금)
    }

    /**
     * 입찰 단위 검증
     */
    public void validateBidUnit(AuctionItem auctionItem, BigDecimal bidAmount) {
        // TODO: 구현 필요
        // 1. 입찰 금액이 입찰 단위의 배수인지 확인
        // 예: 입찰단위 1000원이면 1000원, 2000원, 3000원... 가능
    }

}
