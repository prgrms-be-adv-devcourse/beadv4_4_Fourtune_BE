package com.fourtune.auction.boundedContext.watchlist.application.service;

import com.fourtune.auction.boundedContext.watchlist.domain.constant.WatchlistPolicy;
import com.fourtune.auction.boundedContext.watchlist.domain.entity.Watchlist;
import com.fourtune.auction.boundedContext.watchlist.port.out.WatchlistRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관심상품 공통 조회/검증 기능
 */
@Component
@RequiredArgsConstructor
public class WatchlistSupport {

    private final WatchlistRepository watchlistRepository;

    /**
     * ID로 관심상품 조회 (없으면 예외)
     */
    public Watchlist findByIdOrThrow(Long id) {
        return watchlistRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WATCHLIST_NOT_FOUND));
    }

    /**
     * 사용자 + 경매 ID로 조회 (없으면 예외)
     */
    public Watchlist findByUserIdAndAuctionIdOrThrow(Long userId, Long auctionId) {
        return watchlistRepository.findByUserIdAndAuctionId(userId, auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WATCHLIST_NOT_FOUND));
    }

    /**
     * 관심상품 저장
     */
    public Watchlist save(Watchlist watchlist) {
        return watchlistRepository.save(watchlist);
    }

    /**
     * 관심상품 삭제
     */
    public void delete(Watchlist watchlist) {
        watchlistRepository.delete(watchlist);
    }

    /**
     * 중복 등록 검증
     */
    public void validateNotDuplicate(Long userId, Long auctionId) {
        if (watchlistRepository.existsByUserIdAndAuctionId(userId, auctionId)) {
            throw new BusinessException(ErrorCode.WATCHLIST_ALREADY_EXISTS);
        }
    }

    /**
     * 최대 등록 개수 검증
     */
    public void validateMaxCount(Long userId) {
        long count = watchlistRepository.countByUserId(userId);
        if (count >= WatchlistPolicy.MAX_WATCHLIST_COUNT) {
            throw new BusinessException(ErrorCode.WATCHLIST_LIMIT_EXCEEDED);
        }
    }

    /**
     * 소유자 검증
     */
    public void validateOwner(Watchlist watchlist, Long userId) {
        if (!watchlist.isOwner(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 사용자의 관심상품 목록 조회 (페이징)
     */
    public Page<Watchlist> findByUserId(Long userId, Pageable pageable) {
        return watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자의 관심상품 전체 목록 조회
     */
    public List<Watchlist> findAllByUserId(Long userId) {
        return watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 사용자의 관심상품 개수 조회
     */
    public long countByUserId(Long userId) {
        return watchlistRepository.countByUserId(userId);
    }

    /**
     * 특정 경매의 관심상품 목록 조회
     */
    public List<Watchlist> findByAuctionId(Long auctionId) {
        return watchlistRepository.findByAuctionId(auctionId);
    }

    /**
     * 마감 임박 알림 대상 조회
     */
    public List<Watchlist> findEndingSoonWatchlists() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(WatchlistPolicy.ENDING_SOON_MINUTES);
        return watchlistRepository.findEndingSoonWatchlists(now, threshold);
    }

    /**
     * 가격 변동 알림 대상 조회
     */
    public List<Watchlist> findPriceChangeNotifyTargets(Long auctionId) {
        return watchlistRepository.findByAuctionIdAndNotifyOnPriceChangeTrue(auctionId);
    }

    /**
     * 관심상품 등록 여부 확인
     */
    public boolean existsByUserIdAndAuctionId(Long userId, Long auctionId) {
        return watchlistRepository.existsByUserIdAndAuctionId(userId, auctionId);
    }

}
