package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fourtune.auction.boundedContext.watchList.domain.WatchList;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListAuctionItem;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListUser;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListItemsRepository;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListRepository;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListUserRepository;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchListSupport {

    private final WatchListItemsRepository watchListItemsRepository;
    private final WatchListUserRepository watchListUserRepository;
    private final WatchListRepository watchListRepository;

    public boolean existsByUserIdAndAuctionItemId(Long userId, Long auctionId){
        if(userId == null || auctionId == null)
            throw new BusinessException(ErrorCode.MISSING_INPUT_VALUE);

        return watchListRepository.existsByUserIdAndAuctionItemId(userId, auctionId);
    }

    public WatchListUser findByUserId(Long userId){
        return watchListUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WATCH_LIST_USER_NOT_FOUND));
    }

    public WatchList save(WatchList watchList){
        return watchListRepository.save(watchList);
    }

    public Optional<WatchListUser> findOptionalByUserId(Long userId){
        return watchListUserRepository.findById(userId);
    }

    public WatchListUser saveWatchListUser(WatchListUser watchListUser){
        return watchListUserRepository.save(watchListUser);
    }

    public Optional<WatchListAuctionItem> findOptionalByAuctionItemId(Long auctionItemId){
        return watchListItemsRepository.findById(auctionItemId);
    }

    public List<Long> findAllByAuctionItemId(Long auctionItemId){
        return watchListRepository.findAllByAuctionItemId(auctionItemId);
    }

    public WatchList findWatchListByUserIdAndAuctionItemId(Long auctionItemId, Long userId){
        return watchListRepository.findByUserIdAndAuctionItemId(userId, auctionItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WATCH_LIST_NOT_FOUND));
    }

    public List<WatchList> findAllByUserIdWithFetchJoin(Long userId){
        return watchListRepository.findAllByUserIdWithFetchJoin(userId);
    }

}
