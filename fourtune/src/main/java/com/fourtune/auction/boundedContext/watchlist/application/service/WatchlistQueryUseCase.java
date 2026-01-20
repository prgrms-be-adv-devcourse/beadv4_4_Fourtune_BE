package com.fourtune.auction.boundedContext.watchlist.application.service;

import com.fourtune.auction.boundedContext.watchlist.domain.entity.Watchlist;
import com.fourtune.auction.shared.watchlist.dto.WatchlistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관심상품 조회 UseCase
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchlistQueryUseCase {

    private final WatchlistSupport watchlistSupport;

    /**
     * 관심상품 목록 조회 (페이징)
     */
    public Page<WatchlistResponse> getWatchlist(Long userId, Pageable pageable) {
        return watchlistSupport.findByUserId(userId, pageable)
                .map(WatchlistResponse::from);
    }

    /**
     * 관심상품 전체 목록 조회
     */
    public List<WatchlistResponse> getAllWatchlist(Long userId) {
        return watchlistSupport.findAllByUserId(userId).stream()
                .map(WatchlistResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 관심상품 상세 조회
     */
    public WatchlistResponse getWatchlistDetail(Long watchlistId, Long userId) {
        Watchlist watchlist = watchlistSupport.findByIdOrThrow(watchlistId);
        watchlistSupport.validateOwner(watchlist, userId);
        return WatchlistResponse.from(watchlist);
    }

    /**
     * 관심상품 개수 조회
     */
    public long getWatchlistCount(Long userId) {
        return watchlistSupport.countByUserId(userId);
    }

    /**
     * 관심상품 등록 여부 확인
     */
    public boolean isInWatchlist(Long userId, Long auctionId) {
        return watchlistSupport.existsByUserIdAndAuctionId(userId, auctionId);
    }

}
