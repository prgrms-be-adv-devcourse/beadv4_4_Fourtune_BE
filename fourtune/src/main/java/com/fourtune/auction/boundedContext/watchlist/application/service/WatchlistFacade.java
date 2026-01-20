package com.fourtune.auction.boundedContext.watchlist.application.service;

import com.fourtune.auction.shared.watchlist.dto.WatchlistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관심상품 Facade
 * - 여러 UseCase 조합 및 복잡한 플로우 조율
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WatchlistFacade {

    private final WatchlistAddUseCase watchlistAddUseCase;
    private final WatchlistRemoveUseCase watchlistRemoveUseCase;
    private final WatchlistQueryUseCase watchlistQueryUseCase;

    // ==================== 추가/제거 ====================

    /**
     * 관심상품 추가
     */
    public Long addToWatchlist(Long userId, Long auctionId) {
        return watchlistAddUseCase.addToWatchlist(userId, auctionId);
    }

    /**
     * 관심상품 제거 (ID로)
     */
    public void removeFromWatchlist(Long watchlistId, Long userId) {
        watchlistRemoveUseCase.removeFromWatchlist(watchlistId, userId);
    }

    /**
     * 관심상품 제거 (경매 ID로)
     */
    public void removeByAuctionId(Long userId, Long auctionId) {
        watchlistRemoveUseCase.removeByAuctionId(userId, auctionId);
    }

    // ==================== 조회 ====================

    /**
     * 관심상품 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<WatchlistResponse> getWatchlist(Long userId, Pageable pageable) {
        return watchlistQueryUseCase.getWatchlist(userId, pageable);
    }

    /**
     * 관심상품 전체 목록 조회
     */
    @Transactional(readOnly = true)
    public List<WatchlistResponse> getAllWatchlist(Long userId) {
        return watchlistQueryUseCase.getAllWatchlist(userId);
    }

    /**
     * 관심상품 상세 조회
     */
    @Transactional(readOnly = true)
    public WatchlistResponse getWatchlistDetail(Long watchlistId, Long userId) {
        return watchlistQueryUseCase.getWatchlistDetail(watchlistId, userId);
    }

    /**
     * 관심상품 개수 조회
     */
    @Transactional(readOnly = true)
    public long getWatchlistCount(Long userId) {
        return watchlistQueryUseCase.getWatchlistCount(userId);
    }

    /**
     * 관심상품 등록 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isInWatchlist(Long userId, Long auctionId) {
        return watchlistQueryUseCase.isInWatchlist(userId, auctionId);
    }

}
