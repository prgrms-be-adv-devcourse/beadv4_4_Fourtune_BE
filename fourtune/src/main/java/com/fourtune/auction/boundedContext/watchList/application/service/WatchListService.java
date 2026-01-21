//package com.fourtune.auction.boundedContext.watchList.application.service;
//
//import com.fourtune.auction.boundedContext.watchList.domain.WatchList;
//import com.fourtune.auction.boundedContext.watchList.domain.WatchListAuctionItem;
//import com.fourtune.auction.boundedContext.watchList.domain.WatchListUser;
//import com.fourtune.auction.shared.user.dto.UserResponse;
//import com.fourtune.auction.shared.watchList.dto.WatchListResponseDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class WatchListService {
//
//    private final WatchListSupport watchListSupport;
//    private final WatchListSyncUserUseCase watchListSyncUserUseCase;
//
//    @Transactional
//    public boolean toggleWatchList(Long userId, Long auctionItemId) {
//        if (isExistWatchList(userId, auctionItemId)) {
//            watchListSupport.deleteByUserIdAndAuctionItemId(userId, auctionItemId);
//            return false;
//        }
//        else {
//            WatchListUser user = watchListSupport.findByUserId(userId);
//            WatchListAuctionItem item = watchListSupport.findByAuctionItemId(auctionItemId);
//
//            WatchList watchList = WatchList.builder()
//                    .user(user)
//                    .auctionItem(item)
//                    .build();
//
//            watchListSupport.save(watchList);
//            return true;
//        }
//    }
//
//    public List<WatchListResponseDto> getMyWatchLists(Long userId) {
//        return watchListSupport.findAllByUserId(userId).stream()
//                .map(WatchListResponseDto::from)
//                .collect(Collectors.toList());
//    }
//
//    private boolean isExistWatchList(Long userId, Long itemId) {
//        return watchListSupport.existsByUserIdAndAuctionItemId(userId, itemId);
//    }
//
//    public void syncUser(UserResponse userResponse){
//        watchListSyncUserUseCase.syncUser(userResponse);
//    }
//
//}
