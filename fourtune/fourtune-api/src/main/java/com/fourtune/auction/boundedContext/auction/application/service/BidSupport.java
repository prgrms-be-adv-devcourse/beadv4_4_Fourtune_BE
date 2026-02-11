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
     * ID로 입찰 조회 (예외 발생)
     */
    public Bid findByIdOrThrow(Long bidId) {
        return bidRepository.findById(bidId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BID_NOT_FOUND));
    }

    /**
     * 입찰 저장
     */
    public Bid save(Bid bid) {
        return bidRepository.save(bid);
    }

    /**
     * 경매 ID로 입찰 목록 조회 (최신순)
     */
    public List<Bid> findByAuctionId(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByBidAmountDesc(auctionId);
    }

    /**
     * 경매의 최고가 입찰 조회
     */
    public Optional<Bid> findHighestBid(Long auctionId) {
        return bidRepository.findTopByAuctionIdAndStatusOrderByBidAmountDesc(
                auctionId,
                BidStatus.ACTIVE);
    }

    /**
     * 사용자의 입찰 내역 조회
     */
    public List<Bid> findByBidderId(Long bidderId) {
        return bidRepository.findByBidderIdOrderByCreatedAtDesc(bidderId);
    }

    /**
     * 입찰 가능 여부 검증
     * 현재 최고가보다 높은 금액인지 확인
     */
    public void validateBidAmount(Long auctionId, BigDecimal bidAmount) {
        Optional<Bid> highestBid = findHighestBid(auctionId);
        if (highestBid.isPresent() && bidAmount.compareTo(highestBid.get().getBidAmount()) <= 0) {
            throw new BusinessException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
    }

    /**
     * 경매의 활성 입찰 모두 실패 처리
     * 낙찰 시 최고 입찰자를 제외한 나머지 입찰 실패 처리
     */
    public void failAllActiveBids(Long auctionId) {
        List<Bid> activeBids = bidRepository.findByAuctionIdAndStatus(auctionId, BidStatus.ACTIVE);
        activeBids.forEach(Bid::lose);
        bidRepository.saveAll(activeBids);
    }

}
