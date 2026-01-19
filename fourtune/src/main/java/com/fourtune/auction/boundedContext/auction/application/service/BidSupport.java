package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.boundedContext.auction.port.out.BidRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 입찰 공통 조회/검증 기능
 * - Repository 직접 호출
 * - 여러 UseCase에서 재사용되는 공통 로직
 */
@Component
@RequiredArgsConstructor
public class BidSupport {

    private final BidRepository bidRepository;

    /**
     * ID로 입찰 조회 (Optional)
     */
    public Optional<Bid> findById(Long bidId) {
        // TODO: 구현 필요
        return Optional.empty();
    }

    /**
     * ID로 입찰 조회 (예외 발생)
     */
    public Bid findByIdOrThrow(Long bidId) {
        // TODO: 구현 필요
        // return bidRepository.findById(bidId)
        //         .orElseThrow(() -> new BusinessException(ErrorCode.BID_NOT_FOUND));
        return null;
    }

    /**
     * 입찰 저장
     */
    public Bid save(Bid bid) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 경매 ID로 입찰 목록 조회 (최신순)
     */
    public List<Bid> findByAuctionId(Long auctionId) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 경매의 최고가 입찰 조회
     */
    public Optional<Bid> findHighestBid(Long auctionId) {
        // TODO: 구현 필요
        return Optional.empty();
    }

    /**
     * 사용자의 입찰 내역 조회
     */
    public List<Bid> findByBidderId(Long bidderId) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 입찰 가능 여부 검증
     */
    public void validateBidAmount(Long auctionId, BigDecimal bidAmount) {
        // TODO: 구현 필요
        // 1. 현재 최고가보다 높은지
        // 2. 입찰 단위에 맞는지
        // 3. 시작가보다 높은지
    }

    /**
     * 경매의 활성 입찰 모두 실패 처리
     */
    public void failAllActiveBids(Long auctionId) {
        // TODO: 구현 필요
        // @Modifying @Query 사용
    }

}
