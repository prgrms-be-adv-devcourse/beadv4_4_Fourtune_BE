package com.fourtune.auction.boundedContext.watchlist.application.service;

import com.fourtune.auction.boundedContext.auction.application.service.AuctionSupport;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.watchlist.domain.entity.Watchlist;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관심상품 추가 UseCase
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistAddUseCase {

    private final WatchlistSupport watchlistSupport;
    private final AuctionSupport auctionSupport;

    /**
     * 관심상품 추가
     */
    @Transactional
    public Long addToWatchlist(Long userId, Long auctionId) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 검증
        validateAddable(userId, auctionItem);
        
        // 3. 중복 검증
        watchlistSupport.validateNotDuplicate(userId, auctionId);
        
        // 4. 최대 개수 검증
        watchlistSupport.validateMaxCount(userId);
        
        // 5. 관심상품 생성
        Watchlist watchlist = Watchlist.create(
                userId,
                auctionId,
                auctionItem.getTitle(),
                auctionItem.getCurrentPrice() != null ? auctionItem.getCurrentPrice() : auctionItem.getStartPrice(),
                auctionItem.getAuctionEndTime()
        );
        
        // 6. 저장
        Watchlist saved = watchlistSupport.save(watchlist);
        
        log.info("관심상품 추가: userId={}, auctionId={}", userId, auctionId);
        
        return saved.getId();
    }

    /**
     * 추가 가능 여부 검증
     */
    private void validateAddable(Long userId, AuctionItem auctionItem) {
        // 1. 본인 경매는 추가 불가
        if (auctionItem.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_ADD_OWN_AUCTION);
        }
        
        // 2. 진행 중인 경매만 추가 가능
        if (auctionItem.getStatus() != AuctionStatus.ACTIVE && 
            auctionItem.getStatus() != AuctionStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.WATCHLIST_AUCTION_NOT_ACTIVE);
        }
    }

}
