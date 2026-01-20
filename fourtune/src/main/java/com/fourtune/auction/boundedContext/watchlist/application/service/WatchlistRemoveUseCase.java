package com.fourtune.auction.boundedContext.watchlist.application.service;

import com.fourtune.auction.boundedContext.watchlist.domain.entity.Watchlist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관심상품 제거 UseCase
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistRemoveUseCase {

    private final WatchlistSupport watchlistSupport;

    /**
     * 관심상품 제거 (ID로)
     */
    @Transactional
    public void removeFromWatchlist(Long watchlistId, Long userId) {
        // 1. 관심상품 조회
        Watchlist watchlist = watchlistSupport.findByIdOrThrow(watchlistId);
        
        // 2. 소유자 검증
        watchlistSupport.validateOwner(watchlist, userId);
        
        // 3. 삭제
        watchlistSupport.delete(watchlist);
        
        log.info("관심상품 제거: watchlistId={}, userId={}", watchlistId, userId);
    }

    /**
     * 관심상품 제거 (경매 ID로)
     */
    @Transactional
    public void removeByAuctionId(Long userId, Long auctionId) {
        // 1. 관심상품 조회
        Watchlist watchlist = watchlistSupport.findByUserIdAndAuctionIdOrThrow(userId, auctionId);
        
        // 2. 삭제
        watchlistSupport.delete(watchlist);
        
        log.info("관심상품 제거: userId={}, auctionId={}", userId, auctionId);
    }

}
