package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fourtune.auction.boundedContext.watchList.application.service.performance.WatchListRedisSetUseCase;
import com.fourtune.auction.shared.user.dto.UserResponse;
import com.fourtune.auction.shared.watchList.dto.WatchListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchListService {

    private final WatchListSupport watchListSupport;
    private final WatchListSyncUserUseCase watchListSyncUserUseCase;
    private final WatchListSyncAuctionItemUseCase watchListSyncAuctionItemUseCase;
    private final WatchListRedisSetUseCase watchListRedisSetService;

    @Transactional
    public boolean toggleWatchList(Long userId, Long auctionItemId) {
        if (isExistWatchList(userId, auctionItemId)) {
            watchListRedisSetService.removeInterest(userId, auctionItemId);
            return false;
        }
        else {
            watchListRedisSetService.addInterest(userId, auctionItemId);
            return true;
        }
    }

    public List<WatchListResponseDto> getMyWatchLists(Long userId) {
        return watchListSupport.findAllByUserIdWithFetchJoin(userId).stream()
                .map(WatchListResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void syncUser(UserResponse userResponse){
        watchListSyncUserUseCase.syncUser(userResponse);
    }

    @Transactional
    public void syncAuctionItem(Long auctionItemId, String title, BigDecimal currentPrice, String thumbnailUrl){
        watchListSyncAuctionItemUseCase.syncAuctionItem(auctionItemId, title, currentPrice, thumbnailUrl);
    }

    public void processAuctionStart(Long auctionItemId){
        watchListRedisSetService.processAuctionStart(auctionItemId);
    }

    public void processAuctionEnd(Long auctionItemId){
        watchListRedisSetService.processAuctionEnd(auctionItemId);
    }

    private boolean isExistWatchList(Long userId, Long itemId) {
        return watchListSupport.existsByUserIdAndAuctionItemId(userId, itemId);
    }

}
