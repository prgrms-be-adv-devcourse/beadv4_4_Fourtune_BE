package com.fourtune.auction.boundedContext.watchList.adapter.in.event;

import com.fourtune.auction.boundedContext.watchList.application.service.WatchListService;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.shared.auction.event.AuctionClosedEvent;
import com.fourtune.shared.auction.event.AuctionItemCreatedEvent;
import com.fourtune.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.shared.auction.event.AuctionStartedEvent;
import com.fourtune.shared.user.event.UserDeletedEvent;
import com.fourtune.shared.user.event.UserJoinedEvent;
import com.fourtune.shared.user.event.UserModifiedEvent;
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
    private final EventPublishingConfig eventPublishingConfig;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserJoinEvent(UserJoinedEvent event){
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            log.debug("[WatchList] User 이벤트는 Kafka로 처리됨 - Spring Event 무시");
            return;
        }
        watchListService.syncUser(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserModifiedEvent(UserModifiedEvent event){
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            return;
        }
        watchListService.syncUser(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeletedEvent(UserDeletedEvent event){
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            return;
        }
        watchListService.syncUser(event.getUser());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionItemCreatedEvent(AuctionItemCreatedEvent event){
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            return;
        }
        watchListService.syncAuctionItem(event.auctionItemId(), event.title(), event.currentPrice(), event.thumbnailUrl(), event.category());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionItemModifiedEvent(AuctionItemUpdatedEvent event){
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            return;
        }
        watchListService.syncAuctionItem(event.auctionItemId(), event.title(), event.currentPrice(), event.thumbnailUrl(), event.category());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWatchListItemStartedEvent(AuctionStartedEvent event){
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            return;
        }
        watchListService.processAuctionStart(event.auctionId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWatchListItemEndedEvent(AuctionClosedEvent event){
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            return;
        }
        watchListService.processAuctionEnd(event.auctionId());
    }
}

