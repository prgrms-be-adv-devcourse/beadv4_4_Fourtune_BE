package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fourtune.auction.boundedContext.watchList.domain.WatchList;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListAuctionItem;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListUser;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListItemsRepository;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListRepository;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchListSupport {

    private final WatchListItemsRepository watchListItemsRepository;
    private final WatchListUserRepository watchListUserRepository;
    private final WatchListRepository watchListRepository;

    public boolean existsByUserIdAndAuctionItemId(Long userId, Long auctionId){
        return watchListRepository.existsByUserIdAndAuctionItemId(userId, auctionId);
    }

    public void deleteByUserIdAndAuctionItemId(Long userId, Long auctionId){
        watchListRepository.deleteByUserIdAndAuctionItemId(userId, auctionId);
    }

    public WatchListUser findByUserId(Long userId){
        return watchListUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("동기화된 유저 정보가 없습니다."));
    }

    public WatchListAuctionItem findByAuctionItemId(Long auctionId){
        return watchListItemsRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("동기화된 유저 정보가 없습니다."));
    }

    public WatchList save(WatchList watchList){
        return watchListRepository.save(watchList);
    }

    public List<WatchList> findAllByUserId(Long userId){
        return watchListRepository.findAllByUserId(userId);
    }

}
