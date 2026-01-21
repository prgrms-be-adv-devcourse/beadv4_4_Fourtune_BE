package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.watchList.event.WatchListAuctionEndedEvent;
import com.fourtune.auction.shared.watchList.event.WatchListAuctionStartedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchListAuctionUseCase {

    private final WatchListSupport watchListSupport;
    private final EventPublisher eventPublisher;

    public void findAllByAuctionStartItemId(Long auctionItemId){
        List<Long> watchListUsers = watchListSupport.findAllByAuctionItemId(auctionItemId);
        if(watchListUsers.isEmpty()) return;

        eventPublisher.publish(new WatchListAuctionStartedEvent(watchListUsers, auctionItemId));
    }

    public void findAllByAuctionEndItemId(Long auctionItemId){
        List<Long> watchListUsers = watchListSupport.findAllByAuctionItemId(auctionItemId);
        if(watchListUsers.isEmpty()) return;

        eventPublisher.publish(new WatchListAuctionEndedEvent(watchListUsers, auctionItemId));
    }

}
