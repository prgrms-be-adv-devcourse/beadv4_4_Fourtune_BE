package com.fourtune.auction.boundedContext.watchList.port.out;

import com.fourtune.auction.boundedContext.watchList.domain.WatchListAuctionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchListItemsRepository extends JpaRepository<WatchListAuctionItem, Long> {
}
