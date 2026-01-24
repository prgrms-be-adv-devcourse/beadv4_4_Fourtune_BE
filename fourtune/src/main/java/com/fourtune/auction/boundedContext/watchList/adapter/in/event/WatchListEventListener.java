package com.fourtune.auction.boundedContext.watchList.adapter.in.event;

import com.fourtune.auction.boundedContext.watchList.application.service.WatchListService;
import com.fourtune.auction.shared.auction.event.AuctionClosedEvent;
import com.fourtune.auction.shared.auction.event.AuctionItemCreatedEvent;
import com.fourtune.auction.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.auction.shared.auction.event.AuctionStartedEvent;
import com.fourtune.auction.shared.user.event.UserDeletedEvent;
import com.fourtune.auction.shared.user.event.UserJoinedEvent;
import com.fourtune.auction.shared.user.event.UserModifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchListEventListener {

    private final WatchListService watchListService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserJoinEvent(UserJoinedEvent event){
        watchListService.syncUser(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserModifiedEvent(UserModifiedEvent event){
        watchListService.syncUser(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeletedEvent(UserDeletedEvent event){
        watchListService.syncUser(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionItemCreatedEvent(AuctionItemCreatedEvent event){
        watchListService.syncAuctionItem(event.auctionItemId(), event.title(), event.currentPrice(), event.thumbnailUrl());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionItemModifiedEvent(AuctionItemUpdatedEvent event){
        watchListService.syncAuctionItem(event.auctionItemId(), event.title(), event.currentPrice(), event.thumbnailUrl());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWatchListItemStartedEvent(AuctionStartedEvent event){
        watchListService.findAllByAuctionStartItemId(event.auctionId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWatchListItemEndedEvent(AuctionClosedEvent event){
        watchListService.findAllByAuctionEndItemId(event.auctionId());
    }
}

