package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.boundedContext.auction.mapper.BidMapper;
import com.fourtune.auction.port.out.UserPort;
import com.fourtune.shared.auction.dto.BidDetailResponse;
import com.fourtune.shared.auction.dto.BidHistoryResponse;
import com.fourtune.shared.auction.dto.BidResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 입찰 조회 UseCase
 * - 경매별 입찰 내역 조회
 * - 사용자별 입찰 내역 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidQueryUseCase {

    private final BidSupport bidSupport;
    private final AuctionSupport auctionSupport;
    private final UserPort userPort;

    /**
     * 경매의 입찰 내역 조회
     */
    public BidHistoryResponse getAuctionBids(Long auctionId) {
        // 1. 경매 존재 확인
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        // 2. 입찰 내역 조회 (입찰가 높은순)
        List<Bid> bids = bidSupport.findByAuctionId(auctionId);
        // 3. bidderId 목록으로 닉네임 일괄 조회
        Set<Long> bidderIds = bids.stream().map(Bid::getBidderId).collect(Collectors.toSet());
        var bidderNicknames = userPort.getNicknamesByIds(bidderIds);
        // 4. DTO 변환 후 반환
        return BidMapper.from(auctionItem, bids, bidderNicknames);
    }

    /**
     * 사용자의 입찰 내역 조회
     */
    public List<BidResponse> getUserBids(Long bidderId) {
        List<Bid> bids = bidSupport.findByBidderId(bidderId);
        var nicknames = userPort.getNicknamesByIds(Set.of(bidderId));
        String bidderNickname = nicknames.get(bidderId);

        Set<Long> auctionIds = bids.stream().map(Bid::getAuctionId).collect(Collectors.toSet());
        Map<Long, String> auctionTitles = auctionSupport.findTitlesByIds(auctionIds);

        return bids.stream()
                .map(bid -> BidMapper.from(bid, auctionTitles.get(bid.getAuctionId()), bidderNickname))
                .toList();
    }

    /**
     * 경매의 최고가 입찰 조회
     */
    public BidResponse getHighestBid(Long auctionId) {
        auctionSupport.findByIdOrThrow(auctionId);
        Optional<Bid> highestBid = bidSupport.findHighestBid(auctionId);
        if (highestBid.isEmpty()) {
            return null;
        }
        Bid bid = highestBid.get();
        var nicknames = userPort.getNicknamesByIds(Set.of(bid.getBidderId()));
        return BidMapper.from(bid, nicknames.get(bid.getBidderId()));
    }

    /**
     * 입찰 ID로 상세 조회
     */
    public BidDetailResponse getBidDetail(Long bidId) {
        Bid bid = bidSupport.findByIdOrThrow(bidId);
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(bid.getAuctionId());
        var nicknames = userPort.getNicknamesByIds(Set.of(bid.getBidderId()));
        return BidMapper.from(bid, auctionItem, nicknames.get(bid.getBidderId()));
    }

}
