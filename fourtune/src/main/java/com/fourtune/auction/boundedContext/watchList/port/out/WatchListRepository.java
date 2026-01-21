package com.fourtune.auction.boundedContext.watchList.port.out;

import com.fourtune.auction.boundedContext.watchList.domain.WatchList;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListUser;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WatchListRepository extends JpaRepository<WatchList, Long> {

    boolean existsByUserIdAndAuctionItemId(Long userId, Long auctionItemId);
    void deleteByUserIdAndAuctionItemId(Long userId, Long auctionItemId);

    //@Query("SELECT w FROM WatchList w JOIN FETCH w.watchListAuctionItem WHERE w.user.id = :userId")
    //List<WatchList> findAllByUserIdWithItem(@Param("userId") Long userId);

    List<WatchList> findAllByUserId(Long userId);

    @Query("SELECT w.user.id FROM WatchList w WHERE w.auctionItem.id = :auctionItemId")
    List<Long> findAllByAuctionItemId(Long auctionItemId);

}
